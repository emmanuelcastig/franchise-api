package com.nequi.franchise.r2dbc.config;

import io.r2dbc.postgresql.client.SSLMode;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

@ConfigurationProperties(prefix = "adapters.r2dbc")
public record PostgresProperties(
        String host,
        int port,
        String database,
        String schema,
        SSLMode sslMode,
        Duration connectTimeout,
        Duration statementTimeout,
        Pool pool,
        Credentials credentials) {

    public record Pool(int initialSize, int maxSize, Duration maxIdleTime, Duration maxLifeTime,
                       Duration maxAcquireTime) {
    }

    public record Credentials(String secretName, String username, String password) {

        @Override
        public String toString() {
            return "Credentials[secretName=" + secretName + ", username=" + username + ", password=****]";
        }
    }
}
