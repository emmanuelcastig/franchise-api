package com.nequi.franchise.api;

import com.nequi.franchise.api.error.ErrorHandlingFilter;
import com.nequi.franchise.api.handler.BranchHandler;
import com.nequi.franchise.api.handler.FranchiseHandler;
import com.nequi.franchise.api.handler.ProductHandler;
import com.nequi.franchise.api.validation.RequestValidator;
import com.nequi.franchise.model.branch.Branch;
import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.exceptions.TechnicalException;
import com.nequi.franchise.model.franchise.Franchise;
import com.nequi.franchise.model.product.BranchProductStock;
import com.nequi.franchise.model.product.Product;
import com.nequi.franchise.usecase.branch.BranchUseCase;
import com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import com.nequi.franchise.usecase.product.ProductUseCase;
import com.nequi.franchise.usecase.topstockproduct.TopStockProductUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webflux.test.autoconfigure.WebFluxTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@WebFluxTest
@ContextConfiguration(classes = {RouterRest.class, FranchiseHandler.class, BranchHandler.class, ProductHandler.class,
        RequestValidator.class, ErrorHandlingFilter.class})
class RouterRestTest {

    private static final UUID FRANCHISE_ID = UUID.fromString("4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10");
    private static final UUID BRANCH_ID = UUID.fromString("9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23");
    private static final UUID PRODUCT_ID = UUID.fromString("c3e8a1f2-7b5d-4a9e-b2c4-6d8f0a1e3b57");

    private static final String BRANCHES = "/api/v1/franchises/{franchiseId}/branches";
    private static final String PRODUCTS = BRANCHES + "/{branchId}/products";
    private static final String PRODUCT = PRODUCTS + "/{productId}";

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private FranchiseUseCase franchiseUseCase;
    @MockitoBean
    private TopStockProductUseCase topStockProductUseCase;
    @MockitoBean
    private BranchUseCase branchUseCase;
    @MockitoBean
    private ProductUseCase productUseCase;

    @Nested
    @DisplayName("Franchises")
    class Franchises {

        @Test
        @DisplayName("POST creates a franchise and answers 201")
        void createFranchise() {
            when(franchiseUseCase.create("Burger Town")).thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger Town")));

            webTestClient.post().uri("/api/v1/franchises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Burger Town"))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(FRANCHISE_ID.toString())
                    .jsonPath("$.name").isEqualTo("Burger Town");
        }

        @Test
        @DisplayName("POST with a blank name answers 400 with the violated field, without calling the use case")
        void createFranchiseWithBlankName() {
            webTestClient.post().uri("/api/v1/franchises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", " "))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("FRN-400")
                    .jsonPath("$.errors[0].field").isEqualTo("name")
                    .jsonPath("$.instance").isEqualTo("/api/v1/franchises");

            verifyNoInteractions(franchiseUseCase);
        }

        @Test
        @DisplayName("POST without body answers 400")
        void createFranchiseWithoutBody() {
            webTestClient.post().uri("/api/v1/franchises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors[0].field").isEqualTo("body");
        }

        @Test
        @DisplayName("POST with malformed JSON answers 400")
        void createFranchiseWithMalformedJson() {
            webTestClient.post().uri("/api/v1/franchises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue("{ \"name\": ")
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectHeader().contentType(MediaType.APPLICATION_PROBLEM_JSON)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("FRN-400");
        }

        @Test
        @DisplayName("POST with a taken name answers 409")
        void createFranchiseWithTakenName() {
            when(franchiseUseCase.create("Burger Town"))
                    .thenReturn(Mono.error(new BusinessException(TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS)));

            webTestClient.post().uri("/api/v1/franchises")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Burger Town"))
                    .exchange()
                    .expectStatus().isEqualTo(409)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("FRN-409")
                    .jsonPath("$.detail").isEqualTo(TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS.getMessage());
        }

        @Test
        @DisplayName("PUT name renames the franchise")
        void renameFranchise() {
            when(franchiseUseCase.rename(FRANCHISE_ID, "Burger City"))
                    .thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger City")));

            webTestClient.put().uri("/api/v1/franchises/{franchiseId}/name", FRANCHISE_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Burger City"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("Burger City");
        }

        @Test
        @DisplayName("a malformed identifier answers 400 naming the path variable")
        void renameFranchiseWithInvalidId() {
            webTestClient.put().uri("/api/v1/franchises/{franchiseId}/name", "1-1-1-1-1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Burger City"))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors[0].field").isEqualTo("franchiseId");

            verifyNoInteractions(franchiseUseCase);
        }

        @Test
        @DisplayName("GET top-stock-products lists the best product of every branch")
        void getTopStockProducts() {
            when(topStockProductUseCase.findTopStockProductPerBranch(FRANCHISE_ID)).thenReturn(Flux.just(
                    new BranchProductStock(BRANCH_ID, "Downtown", PRODUCT_ID, "Fries", 120),
                    new BranchProductStock(UUID.randomUUID(), "Uptown", UUID.randomUUID(), "Soda", 80)));

            webTestClient.get().uri("/api/v1/franchises/{franchiseId}/top-stock-products", FRANCHISE_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.length()").isEqualTo(2)
                    .jsonPath("$[0].branchName").isEqualTo("Downtown")
                    .jsonPath("$[0].productName").isEqualTo("Fries")
                    .jsonPath("$[0].stock").isEqualTo(120);
        }

        @Test
        @DisplayName("GET top-stock-products for an unknown franchise answers 404 instead of an empty 200")
        void getTopStockProductsOfUnknownFranchise() {
            when(topStockProductUseCase.findTopStockProductPerBranch(FRANCHISE_ID))
                    .thenReturn(Flux.error(new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)));

            webTestClient.get().uri("/api/v1/franchises/{franchiseId}/top-stock-products", FRANCHISE_ID)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("FRN-404");
        }
    }

    @Nested
    @DisplayName("Branches")
    class Branches {

        @Test
        @DisplayName("POST adds a branch and answers 201")
        void addBranch() {
            when(branchUseCase.addBranch(FRANCHISE_ID, "Downtown"))
                    .thenReturn(Mono.just(new Branch(BRANCH_ID, FRANCHISE_ID, "Downtown")));

            webTestClient.post().uri(BRANCHES, FRANCHISE_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Downtown"))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(BRANCH_ID.toString())
                    .jsonPath("$.franchiseId").isEqualTo(FRANCHISE_ID.toString());
        }

        @Test
        @DisplayName("POST to an unknown franchise answers 404")
        void addBranchToUnknownFranchise() {
            when(branchUseCase.addBranch(FRANCHISE_ID, "Downtown"))
                    .thenReturn(Mono.error(new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)));

            webTestClient.post().uri(BRANCHES, FRANCHISE_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Downtown"))
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("PUT name renames the branch")
        void renameBranch() {
            when(branchUseCase.rename(FRANCHISE_ID, BRANCH_ID, "Uptown"))
                    .thenReturn(Mono.just(new Branch(BRANCH_ID, FRANCHISE_ID, "Uptown")));

            webTestClient.put().uri(BRANCHES + "/{branchId}/name", FRANCHISE_ID, BRANCH_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Uptown"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("Uptown");
        }
    }

    @Nested
    @DisplayName("Products")
    class Products {

        @Test
        @DisplayName("POST adds a product and answers 201")
        void addProduct() {
            when(productUseCase.addProduct(FRANCHISE_ID, BRANCH_ID, "Fries", 25))
                    .thenReturn(Mono.just(new Product(PRODUCT_ID, BRANCH_ID, "Fries", 25)));

            webTestClient.post().uri(PRODUCTS, FRANCHISE_ID, BRANCH_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Fries", "stock", 25))
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody()
                    .jsonPath("$.id").isEqualTo(PRODUCT_ID.toString())
                    .jsonPath("$.stock").isEqualTo(25);
        }

        @Test
        @DisplayName("POST with negative or missing stock answers 400 listing every violation")
        void addProductWithInvalidBody() {
            webTestClient.post().uri(PRODUCTS, FRANCHISE_ID, BRANCH_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "", "stock", -3))
                    .exchange()
                    .expectStatus().isBadRequest()
                    .expectBody()
                    .jsonPath("$.errors.length()").isEqualTo(2)
                    .jsonPath("$.errors[0].field").isEqualTo("name")
                    .jsonPath("$.errors[1].field").isEqualTo("stock");

            verifyNoInteractions(productUseCase);
        }

        @Test
        @DisplayName("DELETE removes the product and answers 204")
        void removeProduct() {
            when(productUseCase.removeProduct(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)).thenReturn(Mono.empty());

            webTestClient.delete().uri(PRODUCT, FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                    .exchange()
                    .expectStatus().isNoContent()
                    .expectBody().isEmpty();
        }

        @Test
        @DisplayName("DELETE of a product outside the branch answers 404")
        void removeMissingProduct() {
            when(productUseCase.removeProduct(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                    .thenReturn(Mono.error(new BusinessException(TechnicalMessage.PRODUCT_NOT_FOUND)));

            webTestClient.delete().uri(PRODUCT, FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                    .exchange()
                    .expectStatus().isNotFound()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("FRN-406");
        }

        @Test
        @DisplayName("PUT stock sets the absolute stock")
        void updateStock() {
            when(productUseCase.updateStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 40))
                    .thenReturn(Mono.just(new Product(PRODUCT_ID, BRANCH_ID, "Fries", 40)));

            webTestClient.put().uri(PRODUCT + "/stock", FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("stock", 40))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.stock").isEqualTo(40);
        }

        @Test
        @DisplayName("PUT name renames the product")
        void renameProduct() {
            when(productUseCase.rename(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Curly Fries"))
                    .thenReturn(Mono.just(new Product(PRODUCT_ID, BRANCH_ID, "Curly Fries", 40)));

            webTestClient.put().uri(PRODUCT + "/name", FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Curly Fries"))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody()
                    .jsonPath("$.name").isEqualTo("Curly Fries");
        }
    }

    @Nested
    @DisplayName("Failures")
    class Failures {

        @Test
        @DisplayName("an unavailable database answers 503 so clients know they can retry")
        void databaseUnavailable() {
            when(productUseCase.updateStock(any(), any(), any(), anyInt()))
                    .thenReturn(Mono.error(new TechnicalException(TechnicalMessage.DATABASE_UNAVAILABLE, new RuntimeException())));

            webTestClient.put().uri(PRODUCT + "/stock", FRANCHISE_ID, BRANCH_ID, PRODUCT_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("stock", 40))
                    .exchange()
                    .expectStatus().isEqualTo(503)
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("FRN-503");
        }

        @Test
        @DisplayName("an unexpected error answers 500 without leaking internal details")
        void unexpectedError() {
            when(franchiseUseCase.rename(any(), anyString()))
                    .thenReturn(Mono.error(new IllegalStateException("connection string leaked")));

            webTestClient.put().uri("/api/v1/franchises/{franchiseId}/name", FRANCHISE_ID)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", "Burger City"))
                    .exchange()
                    .expectStatus().is5xxServerError()
                    .expectBody()
                    .jsonPath("$.code").isEqualTo("FRN-500")
                    .jsonPath("$.detail").isEqualTo(TechnicalMessage.UNEXPECTED_ERROR.getMessage());
        }
    }
}
