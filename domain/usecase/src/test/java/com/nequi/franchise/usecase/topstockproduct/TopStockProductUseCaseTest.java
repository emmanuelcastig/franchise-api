package com.nequi.franchise.usecase.topstockproduct;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.franchise.Franchise;
import com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import com.nequi.franchise.model.product.BranchProductStock;
import com.nequi.franchise.model.product.gateways.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static com.nequi.franchise.usecase.DomainErrorAssertions.isBusinessError;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TopStockProductUseCaseTest {

    private static final UUID FRANCHISE_ID = UUID.randomUUID();
    private static final UUID DOWNTOWN_ID = UUID.randomUUID();
    private static final UUID UPTOWN_ID = UUID.randomUUID();

    @Mock
    private FranchiseRepository franchiseRepository;
    @Mock
    private ProductRepository productRepository;

    private TopStockProductUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new TopStockProductUseCase(franchiseRepository, productRepository);
    }

    private static BranchProductStock stock(UUID branchId, String branchName, String productName, int stock) {
        return new BranchProductStock(branchId, branchName, UUID.randomUUID(), productName, stock);
    }

    private void givenFranchiseExists() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger Town")));
    }

    @Test
    @DisplayName("emits the product with the highest stock of every branch, preserving branch order")
    void emitsTopProductPerBranch() {
        givenFranchiseExists();
        BranchProductStock downtownTop = stock(DOWNTOWN_ID, "Downtown", "Fries", 50);
        BranchProductStock uptownTop = stock(UPTOWN_ID, "Uptown", "Soda", 80);
        when(productRepository.findStocksByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(
                stock(DOWNTOWN_ID, "Downtown", "Cheeseburger", 10),
                downtownTop,
                stock(DOWNTOWN_ID, "Downtown", "Shake", 5),
                stock(UPTOWN_ID, "Uptown", "Burger", 30),
                uptownTop));

        StepVerifier.create(useCase.findTopStockProductPerBranch(FRANCHISE_ID))
                .expectNext(downtownTop, uptownTop)
                .verifyComplete();
    }

    @Test
    @DisplayName("breaks stock ties by product name so the answer is deterministic")
    void breaksTiesByProductName() {
        givenFranchiseExists();
        BranchProductStock apple = stock(DOWNTOWN_ID, "Downtown", "Apple Pie", 20);
        when(productRepository.findStocksByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.just(
                stock(DOWNTOWN_ID, "Downtown", "Brownie", 20),
                apple,
                stock(DOWNTOWN_ID, "Downtown", "Cookie", 20)));

        StepVerifier.create(useCase.findTopStockProductPerBranch(FRANCHISE_ID))
                .expectNext(apple)
                .verifyComplete();
    }

    @Test
    @DisplayName("completes empty when the franchise has no products")
    void completesEmptyWithoutProducts() {
        givenFranchiseExists();
        when(productRepository.findStocksByFranchiseId(FRANCHISE_ID)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.findTopStockProductPerBranch(FRANCHISE_ID))
                .verifyComplete();
    }

    @Test
    @DisplayName("fails with not found when the franchise does not exist")
    void failsWhenFranchiseIsMissing() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.findTopStockProductPerBranch(FRANCHISE_ID))
                .expectErrorMatches(isBusinessError(TechnicalMessage.FRANCHISE_NOT_FOUND))
                .verify();

        verifyNoInteractions(productRepository);
    }

    @Test
    @DisplayName("handles more branches than the default flatMap concurrency without stalling")
    void handlesManyBranches() {
        givenFranchiseExists();
        int branches = 1_000;
        Flux<BranchProductStock> rows = Flux.range(0, branches)
                .concatMap(index -> {
                    UUID branchId = UUID.randomUUID();
                    return Flux.just(
                            stock(branchId, "Branch " + index, "Low", 1),
                            stock(branchId, "Branch " + index, "High", 2));
                });
        when(productRepository.findStocksByFranchiseId(FRANCHISE_ID)).thenReturn(rows);

        StepVerifier.create(useCase.findTopStockProductPerBranch(FRANCHISE_ID))
                .expectNextCount(branches)
                .verifyComplete();
    }

    @Test
    @DisplayName("honours downstream demand: only requested results are produced")
    void honoursBackpressure() {
        givenFranchiseExists();
        Flux<BranchProductStock> rows = Flux.range(0, 10)
                .map(index -> stock(UUID.randomUUID(), "Branch " + index, "Item", index));
        when(productRepository.findStocksByFranchiseId(FRANCHISE_ID)).thenReturn(rows);

        StepVerifier.create(useCase.findTopStockProductPerBranch(FRANCHISE_ID), 2)
                .expectNextCount(2)
                .thenRequest(3)
                .expectNextCount(3)
                .thenCancel()
                .verify();
    }
}
