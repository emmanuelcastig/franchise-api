package com.nequi.franchise.usecase.product;

import com.nequi.franchise.model.branch.Branch;
import com.nequi.franchise.model.branch.gateways.BranchRepository;
import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.product.Product;
import com.nequi.franchise.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.publisher.PublisherProbe;

import java.util.UUID;

import static com.nequi.franchise.usecase.DomainErrorAssertions.isBusinessError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductUseCaseTest {

    private static final UUID FRANCHISE_ID = UUID.randomUUID();
    private static final UUID BRANCH_ID = UUID.randomUUID();
    private static final UUID PRODUCT_ID = UUID.randomUUID();
    private static final Branch BRANCH = new Branch(BRANCH_ID, FRANCHISE_ID, "Downtown");
    private static final Product PRODUCT = new Product(PRODUCT_ID, BRANCH_ID, "Cheeseburger", 10);

    @Mock
    private BranchRepository branchRepository;
    @Mock
    private ProductRepository productRepository;

    private ProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new ProductUseCase(branchRepository, productRepository);
    }

    private void givenBranchExists() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(BRANCH));
    }

    private void givenProductExists() {
        givenBranchExists();
        when(productRepository.findByIdAndBranchId(PRODUCT_ID, BRANCH_ID)).thenReturn(Mono.just(PRODUCT));
    }

    @Nested
    @DisplayName("addProduct")
    class AddProduct {

        @Test
        @DisplayName("stores the product in the branch")
        void storesProduct() {
            givenBranchExists();
            when(productRepository.findByBranchIdAndName(BRANCH_ID, "Cheeseburger")).thenReturn(Mono.empty());
            when(productRepository.save(any())).thenAnswer(invocation -> Mono.just(
                    invocation.<Product>getArgument(0).toBuilder().id(PRODUCT_ID).build()));

            StepVerifier.create(useCase.addProduct(FRANCHISE_ID, BRANCH_ID, " Cheeseburger ", 10))
                    .assertNext(product -> {
                        assertThat(product.id()).isEqualTo(PRODUCT_ID);
                        assertThat(product.branchId()).isEqualTo(BRANCH_ID);
                        assertThat(product.name()).isEqualTo("Cheeseburger");
                        assertThat(product.stock()).isEqualTo(10);
                    })
                    .verifyComplete();
        }

        @Test
        @DisplayName("accepts zero stock")
        void acceptsZeroStock() {
            givenBranchExists();
            when(productRepository.findByBranchIdAndName(BRANCH_ID, "Fries")).thenReturn(Mono.empty());
            when(productRepository.save(any())).thenAnswer(invocation -> Mono.just(invocation.<Product>getArgument(0)));

            StepVerifier.create(useCase.addProduct(FRANCHISE_ID, BRANCH_ID, "Fries", 0))
                    .assertNext(product -> assertThat(product.stock()).isZero())
                    .verifyComplete();
        }

        @Test
        @DisplayName("rejects negative stock before any lookup")
        void rejectsNegativeStock() {
            StepVerifier.create(useCase.addProduct(FRANCHISE_ID, BRANCH_ID, "Fries", -1))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.INVALID_STOCK))
                    .verify();

            verifyNoInteractions(branchRepository, productRepository);
        }

        @Test
        @DisplayName("rejects a blank name")
        void rejectsBlankName() {
            StepVerifier.create(useCase.addProduct(FRANCHISE_ID, BRANCH_ID, " ", 5))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.INVALID_NAME))
                    .verify();
        }

        @Test
        @DisplayName("fails with not found when the branch is not part of the franchise")
        void failsWhenBranchIsMissing() {
            when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(useCase.addProduct(FRANCHISE_ID, BRANCH_ID, "Fries", 5))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.BRANCH_NOT_FOUND))
                    .verify();

            verifyNoInteractions(productRepository);
        }

        @Test
        @DisplayName("fails with conflict when the branch already sells a product with that name")
        void rejectsDuplicatedName() {
            givenBranchExists();
            when(productRepository.findByBranchIdAndName(BRANCH_ID, "Cheeseburger")).thenReturn(Mono.just(PRODUCT));

            StepVerifier.create(useCase.addProduct(FRANCHISE_ID, BRANCH_ID, "Cheeseburger", 3))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS))
                    .verify();

            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("removeProduct")
    class RemoveProduct {

        @Test
        @DisplayName("deletes the product when it belongs to the branch")
        void deletesProduct() {
            givenProductExists();
            PublisherProbe<Void> deletion = PublisherProbe.empty();
            when(productRepository.deleteById(PRODUCT_ID)).thenReturn(deletion.mono());

            StepVerifier.create(useCase.removeProduct(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                    .verifyComplete();

            deletion.assertWasSubscribed();
        }

        @Test
        @DisplayName("fails with not found when the product is not in the branch")
        void failsWhenProductIsMissing() {
            givenBranchExists();
            when(productRepository.findByIdAndBranchId(PRODUCT_ID, BRANCH_ID)).thenReturn(Mono.empty());

            StepVerifier.create(useCase.removeProduct(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.PRODUCT_NOT_FOUND))
                    .verify();

            verify(productRepository, never()).deleteById(any());
        }

        @Test
        @DisplayName("fails with branch not found before looking for the product")
        void failsWhenBranchIsMissing() {
            when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.empty());

            StepVerifier.create(useCase.removeProduct(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.BRANCH_NOT_FOUND))
                    .verify();

            verifyNoInteractions(productRepository);
        }
    }

    @Nested
    @DisplayName("updateStock")
    class UpdateStock {

        @Test
        @DisplayName("sets the new absolute stock")
        void updatesStock() {
            givenProductExists();
            Product updated = PRODUCT.toBuilder().stock(42).build();
            when(productRepository.updateStock(PRODUCT_ID, 42)).thenReturn(Mono.just(updated));

            StepVerifier.create(useCase.updateStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 42))
                    .expectNext(updated)
                    .verifyComplete();
        }

        @Test
        @DisplayName("rejects negative stock without touching the repositories")
        void rejectsNegativeStock() {
            StepVerifier.create(useCase.updateStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, -5))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.INVALID_STOCK))
                    .verify();

            verifyNoInteractions(branchRepository, productRepository);
        }

        @Test
        @DisplayName("fails with not found when the product is deleted concurrently")
        void failsWhenProductDisappears() {
            givenProductExists();
            when(productRepository.updateStock(PRODUCT_ID, 1)).thenReturn(Mono.empty());

            StepVerifier.create(useCase.updateStock(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, 1))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.PRODUCT_NOT_FOUND))
                    .verify();
        }
    }

    @Nested
    @DisplayName("rename")
    class Rename {

        @Test
        @DisplayName("updates the product name")
        void renamesProduct() {
            givenProductExists();
            Product renamed = PRODUCT.toBuilder().name("Double Cheeseburger").build();
            when(productRepository.findByBranchIdAndName(BRANCH_ID, "Double Cheeseburger")).thenReturn(Mono.empty());
            when(productRepository.updateName(PRODUCT_ID, "Double Cheeseburger")).thenReturn(Mono.just(renamed));

            StepVerifier.create(useCase.rename(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Double Cheeseburger"))
                    .expectNext(renamed)
                    .verifyComplete();
        }

        @Test
        @DisplayName("fails with conflict when a sibling product owns the new name")
        void rejectsNameOwnedBySibling() {
            givenProductExists();
            when(productRepository.findByBranchIdAndName(BRANCH_ID, "Fries"))
                    .thenReturn(Mono.just(new Product(UUID.randomUUID(), BRANCH_ID, "Fries", 1)));

            StepVerifier.create(useCase.rename(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, "Fries"))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS))
                    .verify();

            verify(productRepository, never()).updateName(any(), anyString());
        }

        @Test
        @DisplayName("rejects an invalid name")
        void rejectsInvalidName() {
            StepVerifier.create(useCase.rename(FRANCHISE_ID, BRANCH_ID, PRODUCT_ID, null))
                    .expectErrorMatches(isBusinessError(TechnicalMessage.INVALID_NAME))
                    .verify();

            verify(productRepository, never()).updateStock(any(), anyInt());
        }
    }
}
