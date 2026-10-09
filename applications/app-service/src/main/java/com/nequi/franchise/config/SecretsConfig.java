package com.nequi.franchise.config;

import co.com.bancolombia.secretsmanager.config.AWSSecretsManagerConfig;
import co.com.bancolombia.secretsmanager.connector.AWSSecretManagerConnectorAsync;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.regions.Region;

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
