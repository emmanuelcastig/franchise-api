package com.nequi.franchise.r2dbc.config;

import co.com.bancolombia.secretsmanager.connector.AWSSecretManagerConnectorAsync;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CredentialsProviderConfigTest {

    private static final String SECRET_NAME = "rds!db-franchise";

    private final CredentialsProviderConfig config = new CredentialsProviderConfig();

    private static PostgresProperties properties(PostgresProperties.Credentials credentials) {
        return new PostgresProperties("localhost", 5432, "franchise", "public", null, null, null, null, credentials);
    }

    @Test
    @DisplayName("the Secrets Manager provider asks the connector again on every subscription, so rotation is honoured")
    void secretsManagerProviderResolvesOnEverySubscription() {
        AWSSecretManagerConnectorAsync connector = mock(AWSSecretManagerConnectorAsync.class);
        DatabaseCredentials original = new DatabaseCredentials("app_user", "old-password");
        DatabaseCredentials rotated = new DatabaseCredentials("app_user", "new-password");
        when(connector.getSecret(SECRET_NAME, DatabaseCredentials.class))
                .thenReturn(Mono.just(original), Mono.just(rotated));

        Mono<DatabaseCredentials> credentials = config.secretsManagerCredentialsProvider(
                connector, properties(new PostgresProperties.Credentials(SECRET_NAME, null, null))).credentials();

        verify(connector, never()).getSecret(SECRET_NAME, DatabaseCredentials.class);
        StepVerifier.create(credentials).expectNext(original).verifyComplete();
        StepVerifier.create(credentials).expectNext(rotated).verifyComplete();
    }

    @Test
    @DisplayName("the local provider serves the configured credentials")
    void localProviderUsesProperties() {
        DatabaseCredentialsProvider provider = config.localCredentialsProvider(
                properties(new PostgresProperties.Credentials(null, "franchise", "franchise")));

        StepVerifier.create(provider.credentials())
                .expectNext(new DatabaseCredentials("franchise", "franchise"))
                .verifyComplete();
    }

    @Test
    @DisplayName("credentials never print their password")
    void passwordsAreMasked() {
        assertThat(new DatabaseCredentials("app_user", "s3cr3t")).hasToString("DatabaseCredentials[username=app_user, password=****]");
        assertThat(new PostgresProperties.Credentials(SECRET_NAME, "app_user", "s3cr3t").toString()).doesNotContain("s3cr3t");
    }
}
