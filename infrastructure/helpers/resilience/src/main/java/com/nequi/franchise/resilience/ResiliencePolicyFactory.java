package com.nequi.franchise.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiter;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static net.logstash.logback.argument.StructuredArguments.kv;

@Slf4j
@Component
@RequiredArgsConstructor
public class ResiliencePolicyFactory {

    private static final String CIRCUIT_BREAKER_TRANSITION = "Circuit Breaker [STATE TRANSITION]";
    private static final String CIRCUIT_BREAKER_REJECTED = "Circuit Breaker [CALL NOT PERMITTED]";
    private static final String RETRY_ATTEMPT = "Retry [ATTEMPT]";
    private static final String RETRY_EXHAUSTED = "Retry [EXHAUSTED]";
    private static final String TIME_LIMITER_TIMEOUT = "Time Limiter [TIMEOUT]";
    private static final String INSTANCE = "instance";
    private static final String ERROR = "error";

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final RetryRegistry retryRegistry;

    public ResiliencePolicy create(String instanceName) {
        CircuitBreaker circuitBreaker = circuitBreakerRegistry.circuitBreaker(instanceName);
        TimeLimiter timeLimiter = timeLimiterRegistry.timeLimiter(instanceName);
        Retry retry = retryRegistry.retry(instanceName);

        circuitBreaker.getEventPublisher()
                .onStateTransition(event -> log.warn(CIRCUIT_BREAKER_TRANSITION, kv(INSTANCE, instanceName),
                        kv("from", event.getStateTransition().getFromState()),
                        kv("to", event.getStateTransition().getToState())))
                .onCallNotPermitted(event -> log.warn(CIRCUIT_BREAKER_REJECTED, kv(INSTANCE, instanceName)));
        retry.getEventPublisher()
                .onRetry(event -> log.warn(RETRY_ATTEMPT, kv(INSTANCE, instanceName),
                        kv("attempt", event.getNumberOfRetryAttempts()),
                        kv("waitMillis", event.getWaitInterval().toMillis()),
                        kv(ERROR, String.valueOf(event.getLastThrowable()))))
                .onError(event -> log.error(RETRY_EXHAUSTED, kv(INSTANCE, instanceName),
                        kv("attempts", event.getNumberOfRetryAttempts()),
                        kv(ERROR, String.valueOf(event.getLastThrowable()))));
        timeLimiter.getEventPublisher()
                .onTimeout(event -> log.warn(TIME_LIMITER_TIMEOUT, kv(INSTANCE, instanceName)));

        return new ResiliencePolicy(circuitBreaker, timeLimiter, retry);
    }
}
