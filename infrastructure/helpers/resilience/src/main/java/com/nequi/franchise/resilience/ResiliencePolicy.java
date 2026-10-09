package com.nequi.franchise.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.reactor.timelimiter.TimeLimiterOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Decorates calls to an external dependency with Resilience4j operators.
 * <p>
 * Operators are composed with {@code transformDeferred}, so the last one applied is the outermost:
 * {@code Retry( CircuitBreaker( TimeLimiter( call ) ) )}. Every attempt is bounded by the time limiter and
 * recorded by the circuit breaker; once the circuit is open, calls fail fast with {@code CallNotPermittedException},
 * which the retry configuration does not retry.
 */
@RequiredArgsConstructor
public final class ResiliencePolicy {

    private final CircuitBreaker circuitBreaker;
    private final TimeLimiter timeLimiter;
    private final Retry retry;

    /**
     * For operations that are safe to repeat (reads, absolute updates, deletes): timeout, circuit breaker and
     * retry with exponential backoff.
     */
    public <T> Mono<T> idempotent(Mono<T> call) {
        return call
                .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
                .transformDeferred(RetryOperator.of(retry));
    }

    /**
     * For inserts: when a timeout fires the write may already be committed, so retrying could apply it twice.
     */
    public <T> Mono<T> nonIdempotent(Mono<T> call) {
        return call
                .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }

    /**
     * For result streams: re-subscribing after a partial emission would duplicate elements downstream, so streams
     * are not retried. The time limiter bounds the wait between consecutive elements.
     */
    public <T> Flux<T> stream(Flux<T> call) {
        return call
                .transformDeferred(TimeLimiterOperator.of(timeLimiter))
                .transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
    }
}
