package com.nequi.franchise.r2dbc.config;

import co.com.bancolombia.secretsmanager.connector.AWSSecretManagerConnectorAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

@Configuration
public class CredentialsProviderConfig {

    /**
     * Reads the secret through the async connector, which caches it for the configured TTL.
     * Mono.defer makes every subscription (one per new physical connection) ask the connector again, so an expired
     * cache entry is refreshed and rotated credentials are picked up.
     * The concrete connector is injected on purpose: the GenericManagerAsync interface declares a checked
     * SecretException that would force a try-catch, while the implementation reports failures as error signals.
     */
    @Bean
    @Profile("!local")
    public DatabaseCredentialsProvider secretsManagerCredentialsProvider(AWSSecretManagerConnectorAsync connector,
                                                                         PostgresProperties properties) {
        String secretName = properties.credentials().secretName();
        return () -> Mono.defer(() -> connector.getSecret(secretName, DatabaseCredentials.class));
    }

    /**
     * Local development and integration tests only, with credentials of a disposable container.
     */
    @Bean
    @Profile("local")
    public DatabaseCredentialsProvider localCredentialsProvider(PostgresProperties properties) {
        DatabaseCredentials credentials = new DatabaseCredentials(
                properties.credentials().username(), properties.credentials().password());
        return () -> Mono.just(credentials);
    }
}
