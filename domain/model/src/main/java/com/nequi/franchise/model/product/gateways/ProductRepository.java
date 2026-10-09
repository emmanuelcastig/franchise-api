package com.nequi.franchise.model.product.gateways;

import com.nequi.franchise.model.product.BranchProductStock;
import com.nequi.franchise.model.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductRepository {

    Mono<Product> save(Product product);

    /**
     * @return the product when it exists and belongs to the branch, otherwise an empty Mono.
     */
    Mono<Product> findByIdAndBranchId(UUID id, UUID branchId);

    /**
     * @return the product of the branch that owns the given name, or an empty Mono when the name is free.
     */
    Mono<Product> findByBranchIdAndName(UUID branchId, String name);

    /**
     * Updates only the name column.
     *
     * @return the updated product, or an empty Mono when it does not exist.
     */
    Mono<Product> updateName(UUID id, String name);

    /**
     * Sets the stock to an absolute value, which makes the operation idempotent.
     *
     * @return the updated product, or an empty Mono when it does not exist.
     */
    Mono<Product> updateStock(UUID id, int stock);

    Mono<Void> deleteById(UUID id);

    /**
     * Streams every product of the franchise together with its branch.
     * Contract: rows of the same branch are emitted contiguously, so consumers can group them while streaming.
     * Branches without products are not emitted.
     */
    Flux<BranchProductStock> findStocksByFranchiseId(UUID franchiseId);
}
