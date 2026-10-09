package com.nequi.franchise.config;

import co.com.bancolombia.secretsmanager.connector.AWSSecretManagerConnectorAsync;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SecretsConfigTest {

    @Test
    @DisplayName("builds the async Secrets Manager connector without contacting AWS")
    void buildsConnector() {
        AWSSecretManagerConnectorAsync connector = new SecretsConfig()
                .secretsManagerConnector(new SecretsManagerProperties("us-east-1", 300, 10));

        assertThat(connector).isNotNull();
    }
}
