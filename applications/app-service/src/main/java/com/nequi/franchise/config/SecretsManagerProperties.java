package com.nequi.franchise.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "adapters.secrets-manager")
public record SecretsManagerProperties(String region, int cacheSeconds, int cacheSize) {
}
