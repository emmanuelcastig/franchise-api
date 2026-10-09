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

/**
 * Applies the idempotent schema once, while the context starts and before the server accepts traffic.
 * <p>
 * Spring Boot's SQL initializer streams the script from the jar on the thread that acquired the connection,
 * an event-loop thread, which BlockHound reports as blocking I/O. Here the script bytes are loaded eagerly on the
 * main thread, so the reactive pipeline only reads from memory. Disable it with
 * {@code adapters.r2dbc.initialize-schema=false} when migrations run as a separate deployment step.
 */
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
