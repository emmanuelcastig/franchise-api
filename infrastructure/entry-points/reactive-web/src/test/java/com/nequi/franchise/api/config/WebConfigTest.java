package com.nequi.franchise.api.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;

@WebFluxTest
@ContextConfiguration(classes = {CorsConfig.class, SecurityHeadersConfig.class, WebConfigTest.PingRoute.class})
@TestPropertySource(properties = "cors.allowed-origins=https://app.example.com")
class WebConfigTest {

    /**
     * Absolute URL: CORS compares the Origin header with the request host, which a relative URI does not have.
     */
    private static final String API_URL = "https://api.example.com/ping";

    @Autowired
    private WebTestClient webTestClient;

    @Test
    @DisplayName("every response carries the security headers")
    void securityHeaders() {
        webTestClient.get().uri("/ping")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals("Content-Security-Policy", SecurityHeadersConfig.CONTENT_SECURITY_POLICY)
                .expectHeader().valueEquals("Strict-Transport-Security", "max-age=31536000; includeSubDomains; preload")
                .expectHeader().valueEquals("X-Content-Type-Options", "nosniff")
                .expectHeader().doesNotExist("Server")
                .expectHeader().valueEquals("Cache-Control", "no-store")
                .expectHeader().valueEquals("Referrer-Policy", "strict-origin-when-cross-origin");
    }

    @Test
    @DisplayName("CORS preflight allows the configured origin and the methods the API uses")
    void corsPreflightForAllowedOrigin() {
        webTestClient.options().uri(API_URL)
                .header(HttpHeaders.ORIGIN, "https://app.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "PUT")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().valueEquals(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://app.example.com");
    }

    @Test
    @DisplayName("CORS preflight rejects unknown origins")
    void corsPreflightForUnknownOrigin() {
        webTestClient.options().uri(API_URL)
                .header(HttpHeaders.ORIGIN, "https://evil.example.com")
                .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE")
                .exchange()
                .expectStatus().isForbidden();
    }

    @Configuration
    static class PingRoute {

        @Bean
        RouterFunction<ServerResponse> pingRoute() {
            return route(GET("/ping"), request -> ServerResponse.ok().bodyValue("pong"));
        }
    }
}
