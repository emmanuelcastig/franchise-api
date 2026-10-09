package com.nequi.franchise.api.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI franchiseOpenApi(@Value("${spring.application.version:1.0.0}") String version) {
        return new OpenAPI().info(new Info()
                .title("Franchise API")
                .version(version)
                .description("Reactive API to manage franchises, their branches and the stock of their products. "
                        + "Errors follow RFC 9457 (application/problem+json).")
                .contact(new Contact().name("Emmanuel Castillo").url("https://github.com/emmanuelcastig/franchise-api")));
    }
}
