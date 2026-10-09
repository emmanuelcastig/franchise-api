package com.nequi.franchise.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ResiliencePolicyFactory {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final TimeLimiterRegistry timeLimiterRegistry;
    private final RetryRegistry retryRegistry;

    public ResiliencePolicy create(String instanceName) {
        return new ResiliencePolicy(
                circuitBreakerRegistry.circuitBreaker(instanceName),
                timeLimiterRegistry.timeLimiter(instanceName),
                retryRegistry.retry(instanceName));
    }
}
