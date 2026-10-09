package com.nequi.franchise.r2dbc.product;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.product.BranchProductStock;
import com.nequi.franchise.model.product.Product;
import com.nequi.franchise.model.product.gateways.ProductRepository;
import com.nequi.franchise.r2dbc.commons.DatabaseErrors;
import com.nequi.franchise.resilience.ResiliencePolicy;
import io.r2dbc.spi.Readable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static com.nequi.franchise.r2dbc.commons.DatabaseLogs.logFlux;
import static com.nequi.franchise.r2dbc.commons.DatabaseLogs.logMono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    private static final String SAVE = "Save Product";
    private static final String FIND_BY_ID_AND_BRANCH = "Find Product By Id And Branch";
    private static final String FIND_BY_BRANCH_AND_NAME = "Find Product By Branch And Name";
    private static final String UPDATE_NAME = "Update Product Name";
    private static final String UPDATE_STOCK = "Update Product Stock";
    private static final String DELETE = "Delete Product";
    private static final String FIND_STOCKS = "Find Stocks Grouped By Branch";

    private static final String STOCKS_BY_FRANCHISE = """
            SELECT b.id   AS branch_id,
                   b.name AS branch_name,
                   p.id   AS product_id,
                   p.name AS product_name,
                   p.stock
              FROM branch b
              JOIN product p ON p.branch_id = b.id
             WHERE b.franchise_id = :franchiseId
             ORDER BY b.name, b.id
            """;

    private final ProductDataRepository repository;
    private final DatabaseClient databaseClient;
    private final ResiliencePolicy resilience;

    @Override
    public Mono<Product> save(Product product) {
        return resilience.nonIdempotent(repository.save(ProductData.fromDomain(product)))
                .map(ProductData::toDomain)
                .transform(logMono(log, SAVE, product))
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Product> findByIdAndBranchId(UUID id, UUID branchId) {
        return resilience.idempotent(repository.findByIdAndBranchId(id, branchId))
                .map(ProductData::toDomain)
                .transform(logMono(log, FIND_BY_ID_AND_BRANCH, Map.of("id", id, "branchId", branchId)))
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Product> findByBranchIdAndName(UUID branchId, String name) {
        return resilience.idempotent(repository.findByBranchIdAndName(branchId, name))
                .map(ProductData::toDomain)
                .transform(logMono(log, FIND_BY_BRANCH_AND_NAME, Map.of("branchId", branchId, "name", name)))
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Product> updateName(UUID id, String name) {
        return resilience.idempotent(repository.updateName(id, name))
                .map(ProductData::toDomain)
                .transform(logMono(log, UPDATE_NAME, Map.of("id", id, "name", name)))
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Product> updateStock(UUID id, int stock) {
        return resilience.idempotent(repository.updateStock(id, stock))
                .map(ProductData::toDomain)
                .transform(logMono(log, UPDATE_STOCK, Map.of("id", id, "stock", stock)))
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return resilience.idempotent(repository.deleteById(id))
                .transform(logMono(log, DELETE, id))
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Flux<BranchProductStock> findStocksGroupedByBranch(UUID franchiseId) {
        return resilience.stream(databaseClient.sql(STOCKS_BY_FRANCHISE)
                        .bind("franchiseId", franchiseId)
                        .map(ProductRepositoryAdapter::toBranchProductStock)
                        .all())
                .transform(logFlux(log, FIND_STOCKS, franchiseId))
                .onErrorMap(DatabaseErrors::translate);
    }

    private static BranchProductStock toBranchProductStock(Readable row) {
        return BranchProductStock.builder()
                .branchId(row.get("branch_id", UUID.class))
                .branchName(row.get("branch_name", String.class))
                .productId(row.get("product_id", UUID.class))
                .productName(row.get("product_name", String.class))
                .stock(row.get("stock", Integer.class))
                .build();
    }
}
