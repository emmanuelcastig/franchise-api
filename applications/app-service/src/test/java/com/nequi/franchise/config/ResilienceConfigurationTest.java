package com.nequi.franchise.config;

import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.retry.RetryRegistry;
import io.github.resilience4j.timelimiter.TimeLimiterRegistry;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.context.ActiveProfiles;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE, properties = {
        "DB_INITIALIZE_SCHEMA=false",
        "DB_TIMEOUT=750ms",
        "DB_CB_SLIDING_WINDOW_SIZE=40",
        "DB_CB_MIN_NUMBER_CALLS=15",
        "DB_CB_FAILURE_RATE_THRESHOLD=25",
        "DB_CB_SLOW_CALL_DURATION_THRESHOLD=900ms",
        "DB_CB_WAIT_DURATION_OPEN_STATE=30s",
        "DB_RETRY_MAX_ATTEMPTS=5"
})
@ActiveProfiles("local")
class ResilienceConfigurationTest {

    private static final String DATABASE = "database";

    @Autowired
    private CircuitBreakerRegistry circuitBreakers;
    @Autowired
    private TimeLimiterRegistry timeLimiters;
    @Autowired
    private RetryRegistry retries;

    @Test
    @DisplayName("circuit breaker thresholds come from environment variables")
    void circuitBreakerIsConfigurable() {
        CircuitBreakerConfig config = circuitBreakers.circuitBreaker(DATABASE).getCircuitBreakerConfig();

        assertThat(config.getSlidingWindowSize()).isEqualTo(40);
        assertThat(config.getMinimumNumberOfCalls()).isEqualTo(15);
        assertThat(config.getFailureRateThreshold()).isEqualTo(25f);
        assertThat(config.getSlowCallDurationThreshold()).isEqualTo(Duration.ofMillis(900));
        assertThat(config.getWaitIntervalFunctionInOpenState().apply(1)).isEqualTo(30_000L);
        assertThat(config.getIgnoreExceptionPredicate()
                .test(new DuplicateKeyException("uk_franchise_name"))).isTrue();
        assertThat(config.getSlidingWindowSynchronizationStrategy())
                .isEqualTo(CircuitBreakerConfig.SlidingWindowSynchronizationStrategy.LOCK_FREE);
    }

    @Test
    @DisplayName("time limiter and retry come from environment variables")
    void timeoutAndRetryAreConfigurable() {
        assertThat(timeLimiters.timeLimiter(DATABASE).getTimeLimiterConfig().getTimeoutDuration())
                .isEqualTo(Duration.ofMillis(750));
        assertThat(retries.retry(DATABASE).getRetryConfig().getMaxAttempts()).isEqualTo(5);
        assertThat(retries.retry(DATABASE).getRetryConfig().getExceptionPredicate()
                .test(new TimeoutException())).isTrue();
        assertThat(retries.retry(DATABASE).getRetryConfig().getExceptionPredicate()
                .test(new DuplicateKeyException("uk_franchise_name"))).isFalse();
    }
}
