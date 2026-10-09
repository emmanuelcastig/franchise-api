package com.nequi.franchise.r2dbc.config;

import io.r2dbc.spi.ConnectionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

import java.io.IOException;

@Configuration
@ConditionalOnProperty(prefix = "adapters.r2dbc", name = "initialize-schema", havingValue = "true", matchIfMissing = true)
public class SchemaInitializerConfig {

    @Bean
    public ConnectionFactoryInitializer schemaInitializer(ConnectionFactory connectionFactory,
                                                          @Value("classpath:db/schema.sql") Resource schema)
            throws IOException {
        ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
        initializer.setConnectionFactory(connectionFactory);
        initializer.setDatabasePopulator(new ResourceDatabasePopulator(new ByteArrayResource(schema.getContentAsByteArray())));
        return initializer;
    }
}
