package com.nequi.franchise;

import com.nequi.franchise.api.dto.response.BranchResponse;
import com.nequi.franchise.api.dto.response.FranchiseResponse;
import com.nequi.franchise.api.dto.response.ProductResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webtestclient.autoconfigure.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.Map;
import java.util.UUID;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("local")
@Testcontainers(disabledWithoutDocker = true)
class FranchiseApiEndToEndTest {

    private static final String FRANCHISES = "/api/v1/franchises";
    private static final String BRANCHES = FRANCHISES + "/{franchiseId}/branches";
    private static final String PRODUCTS = BRANCHES + "/{branchId}/products";
    private static final String PRODUCT = PRODUCTS + "/{productId}";

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:17-alpine");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("adapters.r2dbc.host", POSTGRES::getHost);
        registry.add("adapters.r2dbc.port", () -> POSTGRES.getMappedPort(PostgreSQLContainer.POSTGRESQL_PORT));
        registry.add("adapters.r2dbc.database", POSTGRES::getDatabaseName);
        registry.add("adapters.r2dbc.credentials.username", POSTGRES::getUsername);
        registry.add("adapters.r2dbc.credentials.password", POSTGRES::getPassword);
    }

    @Autowired
    private WebTestClient client;

    @Test
    @DisplayName("a franchise can be built, edited and queried through the public API")
    void fullFranchiseLifecycle() {
        UUID franchiseId = createFranchise("Burger Town").id();

        client.post().uri(FRANCHISES).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Burger Town"))
                .exchange()
                .expectStatus().isEqualTo(409);

        UUID downtownId = addBranch(franchiseId, "Downtown").id();
        UUID uptownId = addBranch(franchiseId, "Uptown").id();
        addBranch(franchiseId, "Empty Branch");

        addProduct(franchiseId, downtownId, "Fries", 50);
        UUID shakeId = addProduct(franchiseId, downtownId, "Shake", 5).id();
        UUID sodaId = addProduct(franchiseId, uptownId, "Soda", 80).id();
        UUID burgerId = addProduct(franchiseId, uptownId, "Burger", 30).id();

        client.put().uri(PRODUCT + "/stock", franchiseId, downtownId, shakeId).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("stock", 90))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.stock").isEqualTo(90);

        client.put().uri(PRODUCT + "/name", franchiseId, uptownId, burgerId).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Double Burger"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.name").isEqualTo("Double Burger");

        client.put().uri(BRANCHES + "/{branchId}/name", franchiseId, uptownId).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Midtown"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.name").isEqualTo("Midtown");

        client.put().uri(FRANCHISES + "/{franchiseId}/name", franchiseId).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Burger City"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.name").isEqualTo("Burger City");

        client.get().uri(FRANCHISES + "/{franchiseId}/top-stock-products", franchiseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].branchName").isEqualTo("Downtown")
                .jsonPath("$[0].productName").isEqualTo("Shake")
                .jsonPath("$[0].stock").isEqualTo(90)
                .jsonPath("$[1].branchName").isEqualTo("Midtown")
                .jsonPath("$[1].productName").isEqualTo("Soda");

        client.delete().uri(PRODUCT, franchiseId, uptownId, sodaId)
                .exchange()
                .expectStatus().isNoContent();

        client.delete().uri(PRODUCT, franchiseId, uptownId, sodaId)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("FRN-406");

        client.get().uri(FRANCHISES + "/{franchiseId}/top-stock-products", franchiseId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[1].productName").isEqualTo("Double Burger")
                .jsonPath("$[1].stock").isEqualTo(30);
    }

    @Test
    @DisplayName("a branch of another franchise cannot be used to reach its products")
    void hierarchyIsEnforced() {
        UUID franchiseId = createFranchise("Pizza Town").id();
        UUID otherFranchiseId = createFranchise("Taco Town").id();
        UUID branchId = addBranch(franchiseId, "Center").id();

        client.post().uri(PRODUCTS, otherFranchiseId, branchId).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", "Pepperoni", "stock", 10))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody().jsonPath("$.code").isEqualTo("FRN-405");
    }

    @Test
    @DisplayName("the OpenAPI document describes the nine operations")
    void openApiDocumentListsEveryOperation() {
        client.get().uri("/v3/api-docs")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.paths.length()").isEqualTo(9)
                .jsonPath("$.paths['/api/v1/franchises'].post.operationId").isEqualTo("createFranchise")
                .jsonPath("$.paths['/api/v1/franchises/{franchiseId}/top-stock-products'].get.operationId")
                .isEqualTo("getTopStockProducts");
    }

    @Test
    @DisplayName("the readiness probe used by the load balancer reports UP")
    void readinessProbe() {
        client.get().uri("/actuator/health/readiness")
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.status").isEqualTo("UP");
    }

    private FranchiseResponse createFranchise(String name) {
        return client.post().uri(FRANCHISES).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", name))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(FranchiseResponse.class)
                .returnResult().getResponseBody();
    }

    private BranchResponse addBranch(UUID franchiseId, String name) {
        return client.post().uri(BRANCHES, franchiseId).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", name))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(BranchResponse.class)
                .returnResult().getResponseBody();
    }

    private ProductResponse addProduct(UUID franchiseId, UUID branchId, String name, int stock) {
        return client.post().uri(PRODUCTS, franchiseId, branchId).contentType(MediaType.APPLICATION_JSON)
                .bodyValue(Map.of("name", name, "stock", stock))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ProductResponse.class)
                .returnResult().getResponseBody();
    }
}
