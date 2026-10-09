package com.nequi.franchise.r2dbc.config;

import com.nequi.franchise.resilience.ResiliencePolicy;
import com.nequi.franchise.resilience.ResiliencePolicyFactory;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowSynchronizationStrategy;
import io.github.resilience4j.common.circuitbreaker.configuration.CircuitBreakerConfigCustomizer;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import io.r2dbc.spi.ValidationDepth;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class PostgresConnectionPoolConfig {

    public static final String DATABASE_RESILIENCE_INSTANCE = "database";
    private static final String APPLICATION_NAME = "franchise-api";

    @Bean
    public ConnectionPool connectionPool(PostgresProperties properties, DatabaseCredentialsProvider credentialsProvider) {
        Mono<DatabaseCredentials> credentials = credentialsProvider.credentials();

        PostgresqlConnectionConfiguration connectionConfiguration = PostgresqlConnectionConfiguration.builder()
                .applicationName(APPLICATION_NAME)
                .host(properties.host())
                .port(properties.port())
                .database(properties.database())
                .schema(properties.schema())
                .sslMode(properties.sslMode())
                .connectTimeout(properties.connectTimeout())
                .statementTimeout(properties.statementTimeout())
                .username(credentials.map(DatabaseCredentials::username))
                .password(credentials.<CharSequence>map(DatabaseCredentials::password))
                .build();

        PostgresProperties.Pool pool = properties.pool();
        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration
                .builder(new PostgresqlConnectionFactory(connectionConfiguration))
                .name(APPLICATION_NAME + "-pool")
                .initialSize(pool.initialSize())
                .maxSize(pool.maxSize())
                .maxIdleTime(pool.maxIdleTime())
                .maxLifeTime(pool.maxLifeTime())
                .maxAcquireTime(pool.maxAcquireTime())
                .validationDepth(ValidationDepth.REMOTE)
                .build();

        return new ConnectionPool(poolConfiguration);
    }

    @Bean
    public CircuitBreakerConfigCustomizer databaseCircuitBreakerCustomizer() {
        return CircuitBreakerConfigCustomizer.of(DATABASE_RESILIENCE_INSTANCE,
                builder -> builder.slidingWindowSynchronizationStrategy(SlidingWindowSynchronizationStrategy.LOCK_FREE));
    }

    @Bean
    public ResiliencePolicy databaseResiliencePolicy(ResiliencePolicyFactory factory) {
        return factory.create(DATABASE_RESILIENCE_INSTANCE);
    }
}
