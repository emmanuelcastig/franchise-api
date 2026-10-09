package com.nequi.franchise.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ResiliencePolicyTest {

    private CircuitBreaker circuitBreaker;
    private ResiliencePolicy policy;
    private final AtomicInteger attempts = new AtomicInteger();

    @BeforeEach
    void setUp() {
        circuitBreaker = CircuitBreaker.of("test", CircuitBreakerConfig.custom()
                .slidingWindowSize(4)
                .minimumNumberOfCalls(4)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofMinutes(1))
                .build());
        TimeLimiter timeLimiter = TimeLimiter.of(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(200))
                .build());
        Retry retry = Retry.of("test", RetryConfig.custom()
                .maxAttempts(3)
                .waitDuration(Duration.ofMillis(10))
                .retryExceptions(IllegalStateException.class, TimeoutException.class)
                .build());
        policy = new ResiliencePolicy(circuitBreaker, timeLimiter, retry);
    }

    private Mono<String> failingTimes(int failures, RuntimeException error) {
        return Mono.defer(() -> attempts.incrementAndGet() <= failures ? Mono.error(error) : Mono.just("ok"));
    }

    @Test
    @DisplayName("idempotent calls are retried on transient failures until they succeed")
    void idempotentRetriesTransientFailures() {
        StepVerifier.create(policy.idempotent(failingTimes(2, new IllegalStateException("transient"))))
                .expectNext("ok")
                .verifyComplete();

        assertThat(attempts).hasValue(3);
    }

    @Test
    @DisplayName("idempotent calls give up after the configured attempts and surface the last error")
    void idempotentStopsAfterMaxAttempts() {
        StepVerifier.create(policy.idempotent(failingTimes(5, new IllegalStateException("down"))))
                .expectErrorMessage("down")
                .verify();

        assertThat(attempts).hasValue(3);
    }

    @Test
    @DisplayName("idempotent calls are not retried for errors outside the retry list")
    void idempotentDoesNotRetryNonTransientErrors() {
        StepVerifier.create(policy.idempotent(failingTimes(1, new IllegalArgumentException("bad input"))))
                .expectError(IllegalArgumentException.class)
                .verify();

        assertThat(attempts).hasValue(1);
    }

    @Test
    @DisplayName("non idempotent calls are never retried, even on transient failures")
    void nonIdempotentIsNeverRetried() {
        StepVerifier.create(policy.nonIdempotent(failingTimes(1, new IllegalStateException("transient"))))
                .expectError(IllegalStateException.class)
                .verify();

        assertThat(attempts).hasValue(1);
    }

    @Test
    @DisplayName("streams are not retried, so already emitted elements are never duplicated")
    void streamsAreNotRetried() {
        Flux<String> partiallyFailing = Flux.defer(() -> {
            attempts.incrementAndGet();
            return Flux.just("a", "b").concatWith(Flux.error(new IllegalStateException("broken stream")));
        });

        StepVerifier.create(policy.stream(partiallyFailing))
                .expectNext("a", "b")
                .expectError(IllegalStateException.class)
                .verify();

        assertThat(attempts).hasValue(1);
    }

    @Test
    @DisplayName("the time limiter cancels calls that take longer than the configured timeout")
    void timeLimiterCancelsSlowCalls() {
        PublisherProbe<String> slowCall = PublisherProbe.of(Mono.never());

        StepVerifier.create(policy.nonIdempotent(slowCall.mono()))
                .expectError(TimeoutException.class)
                .verify(Duration.ofSeconds(2));

        slowCall.assertWasCancelled();
    }

    @Test
    @DisplayName("an open circuit rejects calls without subscribing to them")
    void openCircuitFailsFast() {
        circuitBreaker.transitionToOpenState();
        PublisherProbe<String> call = PublisherProbe.of(Mono.just("ok"));

        StepVerifier.create(policy.idempotent(call.mono()))
                .expectError(CallNotPermittedException.class)
                .verify();

        call.assertWasNotSubscribed();
    }

    @Test
    @DisplayName("repeated failures open the circuit")
    void repeatedFailuresOpenTheCircuit() {
        Mono<String> alwaysFailing = Mono.error(new IllegalArgumentException("boom"));

        Flux.range(0, 4)
                .concatMap(index -> policy.nonIdempotent(alwaysFailing).onErrorResume(error -> Mono.empty()))
                .as(StepVerifier::create)
                .verifyComplete();

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
