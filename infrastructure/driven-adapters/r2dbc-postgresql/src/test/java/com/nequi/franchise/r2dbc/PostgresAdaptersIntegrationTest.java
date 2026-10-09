package com.nequi.franchise.r2dbc;

import com.nequi.franchise.model.branch.Branch;
import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.franchise.Franchise;
import com.nequi.franchise.model.product.BranchProductStock;
import com.nequi.franchise.model.product.Product;
import com.nequi.franchise.r2dbc.branch.BranchRepositoryAdapter;
import com.nequi.franchise.r2dbc.franchise.FranchiseRepositoryAdapter;
import com.nequi.franchise.r2dbc.product.ProductRepositoryAdapter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = PostgresAdaptersIntegrationTest.TestApplication.class)
@ActiveProfiles("local")
@Testcontainers(disabledWithoutDocker = true)
class PostgresAdaptersIntegrationTest {

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
    private FranchiseRepositoryAdapter franchises;
    @Autowired
    private BranchRepositoryAdapter branches;
    @Autowired
    private ProductRepositoryAdapter products;

    private static String unique(String prefix) {
        return prefix + " " + UUID.randomUUID();
    }

    private Mono<Franchise> newFranchise() {
        return franchises.save(Franchise.builder().name(unique("Franchise")).build());
    }

    private Mono<Branch> newBranch(UUID franchiseId, String name) {
        return branches.save(Branch.builder().franchiseId(franchiseId).name(name).build());
    }

    private Mono<Product> newProduct(UUID branchId, String name, int stock) {
        return products.save(Product.builder().branchId(branchId).name(name).stock(stock).build());
    }

    @Test
    @DisplayName("franchises get a database generated id and can be found by id and by name")
    void franchiseRoundTrip() {
        String name = unique("Burger Town");

        StepVerifier.create(franchises.save(Franchise.builder().name(name).build())
                        .flatMap(saved -> franchises.findById(saved.id()).zipWith(franchises.findByName(name))))
                .assertNext(found -> {
                    assertThat(found.getT1().id()).isNotNull();
                    assertThat(found.getT1()).isEqualTo(found.getT2());
                    assertThat(found.getT1().name()).isEqualTo(name);
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("the unique constraint rejects a duplicated franchise name with a business conflict")
    void duplicatedFranchiseNameIsRejectedByTheDatabase() {
        String name = unique("Duplicated");

        StepVerifier.create(franchises.save(Franchise.builder().name(name).build())
                        .then(franchises.save(Franchise.builder().name(name).build())))
                .expectErrorMatches(error -> error instanceof BusinessException business
                        && business.getTechnicalMessage() == TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS)
                .verify();
    }

    @Test
    @DisplayName("updateName changes only the targeted franchise and returns empty for unknown ids")
    void franchiseUpdateName() {
        String newName = unique("Renamed");

        StepVerifier.create(newFranchise().flatMap(saved -> franchises.updateName(saved.id(), newName)))
                .assertNext(updated -> assertThat(updated.name()).isEqualTo(newName))
                .verifyComplete();

        StepVerifier.create(franchises.updateName(UUID.randomUUID(), unique("Ghost")))
                .verifyComplete();
    }

    @Test
    @DisplayName("branches are scoped to their franchise")
    void branchesAreScopedToTheirFranchise() {
        Mono<Boolean> scenario = newFranchise().zipWith(newFranchise())
                .flatMap(pair -> newBranch(pair.getT1().id(), "Downtown")
                        .flatMap(branch -> branches.findByIdAndFranchiseId(branch.id(), pair.getT1().id())
                                .zipWith(branches.findByIdAndFranchiseId(branch.id(), pair.getT2().id()).hasElement())
                                .map(result -> result.getT1().equals(branch) && !result.getT2())));

        StepVerifier.create(scenario)
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("two franchises may have branches with the same name, but one franchise may not")
    void branchNameUniquenessIsPerFranchise() {
        StepVerifier.create(newFranchise().zipWith(newFranchise())
                        .flatMap(pair -> newBranch(pair.getT1().id(), "Downtown")
                                .then(newBranch(pair.getT2().id(), "Downtown"))
                                .then(newBranch(pair.getT1().id(), "Downtown"))))
                .expectErrorMatches(error -> error instanceof BusinessException business
                        && business.getTechnicalMessage() == TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS)
                .verify();
    }

    @Test
    @DisplayName("stock and name updates touch a single column and return the updated product")
    void productUpdates() {
        Mono<Product> scenario = newFranchise()
                .flatMap(franchise -> newBranch(franchise.id(), "Downtown"))
                .flatMap(branch -> newProduct(branch.id(), "Fries", 5))
                .flatMap(product -> products.updateStock(product.id(), 42))
                .flatMap(product -> products.updateName(product.id(), "Curly Fries"));

        StepVerifier.create(scenario)
                .assertNext(product -> {
                    assertThat(product.stock()).isEqualTo(42);
                    assertThat(product.name()).isEqualTo("Curly Fries");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("deleted products can no longer be found in their branch")
    void productDeletion() {
        Mono<Boolean> scenario = newFranchise()
                .flatMap(franchise -> newBranch(franchise.id(), "Downtown"))
                .flatMap(branch -> newProduct(branch.id(), "Fries", 5)
                        .flatMap(product -> products.deleteById(product.id())
                                .then(products.findByIdAndBranchId(product.id(), branch.id()).hasElement())));

        StepVerifier.create(scenario)
                .expectNext(false)
                .verifyComplete();
    }

    @Test
    @DisplayName("stocks are streamed grouped by branch, skipping branches without products")
    void stocksAreGroupedByBranch() {
        Mono<UUID> franchiseWithData = newFranchise()
                .flatMap(franchise -> newBranch(franchise.id(), "B - Uptown")
                        .flatMap(uptown -> newProduct(uptown.id(), "Soda", 80).then(newProduct(uptown.id(), "Burger", 30)))
                        .then(newBranch(franchise.id(), "A - Downtown"))
                        .flatMap(downtown -> newProduct(downtown.id(), "Fries", 50).then(newProduct(downtown.id(), "Shake", 5)))
                        .then(newBranch(franchise.id(), "C - Empty"))
                        .thenReturn(franchise.id()));

        StepVerifier.create(franchiseWithData.flatMapMany(products::findStocksGroupedByBranch)
                        .map(BranchProductStock::branchName))
                .expectNext("A - Downtown", "A - Downtown", "B - Uptown", "B - Uptown")
                .verifyComplete();
    }

    @SpringBootConfiguration
    @EnableAutoConfiguration
    @ConfigurationPropertiesScan("com.nequi.franchise")
    @ComponentScan("com.nequi.franchise")
    static class TestApplication {
    }
}
