package com.nequi.franchise.r2dbc.product;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.product.BranchProductStock;
import com.nequi.franchise.model.product.Product;
import com.nequi.franchise.model.product.gateways.ProductRepository;
import com.nequi.franchise.r2dbc.commons.DatabaseErrors;
import com.nequi.franchise.resilience.ResiliencePolicy;
import io.r2dbc.spi.Readable;
import lombok.RequiredArgsConstructor;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class ProductRepositoryAdapter implements ProductRepository {

    /**
     * Ordered by branch so rows of the same branch are contiguous, as the port contract requires.
     * The unique index (branch_id, name) on product and (franchise_id, name) on branch back this join.
     */
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
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Product> findByIdAndBranchId(UUID id, UUID branchId) {
        return resilience.idempotent(repository.findByIdAndBranchId(id, branchId))
                .map(ProductData::toDomain)
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Product> findByBranchIdAndName(UUID branchId, String name) {
        return resilience.idempotent(repository.findByBranchIdAndName(branchId, name))
                .map(ProductData::toDomain)
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Product> updateName(UUID id, String name) {
        return resilience.idempotent(repository.updateName(id, name))
                .map(ProductData::toDomain)
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Product> updateStock(UUID id, int stock) {
        return resilience.idempotent(repository.updateStock(id, stock))
                .map(ProductData::toDomain)
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Void> deleteById(UUID id) {
        return resilience.idempotent(repository.deleteById(id))
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Flux<BranchProductStock> findStocksByFranchiseId(UUID franchiseId) {
        return resilience.stream(databaseClient.sql(STOCKS_BY_FRANCHISE)
                        .bind("franchiseId", franchiseId)
                        .map(ProductRepositoryAdapter::toBranchProductStock)
                        .all())
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
