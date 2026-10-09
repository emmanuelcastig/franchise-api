package com.nequi.franchise.r2dbc.config;

import co.com.bancolombia.secretsmanager.connector.AWSSecretManagerConnectorAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import reactor.core.publisher.Mono;

@Configuration
public class CredentialsProviderConfig {

    @Bean
    @Profile("!local")
    public DatabaseCredentialsProvider secretsManagerCredentialsProvider(AWSSecretManagerConnectorAsync connector,
                                                                         PostgresProperties properties) {
        String secretName = properties.credentials().secretName();
        return () -> Mono.defer(() -> connector.getSecret(secretName, DatabaseCredentials.class));
    }

    @Bean
    @Profile("local")
    public DatabaseCredentialsProvider localCredentialsProvider(PostgresProperties properties) {
        DatabaseCredentials credentials = new DatabaseCredentials(
                properties.credentials().username(), properties.credentials().password());
        return () -> Mono.just(credentials);
    }
}
