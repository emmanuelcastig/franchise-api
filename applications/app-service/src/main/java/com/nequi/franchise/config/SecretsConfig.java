package com.nequi.franchise.config;

import co.com.bancolombia.secretsmanager.config.AWSSecretsManagerConfig;
import co.com.bancolombia.secretsmanager.connector.AWSSecretManagerConnectorAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;

/**
 * The connector builds an async AWS client that authenticates with the default provider chain; on ECS that is
 * the task role, so no AWS keys exist anywhere in the configuration.
 */
@Configuration
@Profile("!local")
public class SecretsConfig {

    @Bean
    public AWSSecretManagerConnectorAsync secretsManagerConnector(SecretsManagerProperties properties) {
        return new AWSSecretManagerConnectorAsync(AWSSecretsManagerConfig.builder()
                .region(Region.of(properties.region()))
                .cacheSeconds(properties.cacheSeconds())
                .cacheSize(properties.cacheSize())
                .build());
    }
}
