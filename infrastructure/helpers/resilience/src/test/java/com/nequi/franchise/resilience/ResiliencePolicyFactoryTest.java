package com.nequi.franchise.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

class ResiliencePolicyFactoryTest {

    @Test
    @DisplayName("creates a policy backed by the registry instances of the given name")
    void createsPolicyFromRegistries() {
        CircuitBreakerRegistry circuitBreakers = CircuitBreakerRegistry.ofDefaults();
        TimeLimiterRegistry timeLimiters = TimeLimiterRegistry.ofDefaults();
        RetryRegistry retries = RetryRegistry.ofDefaults();

        ResiliencePolicy policy = new ResiliencePolicyFactory(circuitBreakers, timeLimiters, retries).create("database");

        StepVerifier.create(policy.idempotent(Mono.just("ok")))
                .expectNext("ok")
                .verifyComplete();
        assertThat(circuitBreakers.find("database")).isPresent();
        assertThat(timeLimiters.find("database")).isPresent();
        assertThat(retries.find("database")).isPresent();
        assertThat(circuitBreakers.circuitBreaker("database").getMetrics().getNumberOfSuccessfulCalls()).isEqualTo(1);
    }
}
