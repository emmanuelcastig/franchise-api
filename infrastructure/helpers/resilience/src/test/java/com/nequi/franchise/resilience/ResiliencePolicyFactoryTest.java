package com.nequi.franchise.resilience;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterConfig;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class ResiliencePolicyFactoryTest {

    private static final String DATABASE = "database";

    private CircuitBreakerRegistry circuitBreakers;
    private ResiliencePolicy policy;

    @BeforeEach
    void setUp() {
        circuitBreakers = CircuitBreakerRegistry.ofDefaults();
        TimeLimiterRegistry timeLimiters = TimeLimiterRegistry.of(TimeLimiterConfig.custom()
                .timeoutDuration(Duration.ofMillis(100))
                .build());
        RetryRegistry retries = RetryRegistry.of(RetryConfig.custom()
                .maxAttempts(2)
                .waitDuration(Duration.ofMillis(10))
                .retryExceptions(IllegalStateException.class)
                .build());
        policy = new ResiliencePolicyFactory(circuitBreakers, timeLimiters, retries).create(DATABASE);
    }

    @Test
    @DisplayName("creates a policy backed by the registry instances of the given name")
    void createsPolicyFromRegistries() {
        StepVerifier.create(policy.idempotent(Mono.just("ok")))
                .expectNext("ok")
                .verifyComplete();

        assertThat(circuitBreakers.circuitBreaker(DATABASE).getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(1);
    }

    @Test
    @DisplayName("retries are attempted and reported until they are exhausted")
    void retriesAreReported() {
        AtomicInteger attempts = new AtomicInteger();

        StepVerifier.create(policy.idempotent(Mono.defer(() -> {
                    attempts.incrementAndGet();
                    return Mono.error(new IllegalStateException("transient"));
                })))
                .expectError(IllegalStateException.class)
                .verify();

        assertThat(attempts).hasValue(2);
    }

    @Test
    @DisplayName("timeouts are reported by the time limiter")
    void timeoutsAreReported() {
        StepVerifier.create(policy.nonIdempotent(Mono.never()))
                .expectError(TimeoutException.class)
                .verify(Duration.ofSeconds(2));
    }

    @Test
    @DisplayName("state transitions and rejected calls are reported by the circuit breaker")
    void circuitBreakerEventsAreReported() {
        CircuitBreaker circuitBreaker = circuitBreakers.circuitBreaker(DATABASE);
        circuitBreaker.transitionToOpenState();

        StepVerifier.create(policy.nonIdempotent(Mono.just("ok")))
                .expectError(CallNotPermittedException.class)
                .verify();

        assertThat(circuitBreaker.getState()).isEqualTo(CircuitBreaker.State.OPEN);
    }
}
