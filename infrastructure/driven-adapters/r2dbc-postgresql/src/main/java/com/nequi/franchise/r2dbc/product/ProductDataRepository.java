package com.nequi.franchise.r2dbc.product;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductDataRepository extends ReactiveCrudRepository<ProductData, UUID> {

    Mono<ProductData> findByIdAndBranchId(UUID id, UUID branchId);

    Mono<ProductData> findByBranchIdAndName(UUID branchId, String name);

    @Query("""
            UPDATE product
               SET name = :name, updated_at = now()
             WHERE id = :id
            RETURNING id, branch_id, name, stock
            """)
    Mono<ProductData> updateName(UUID id, String name);

    @Query("""
            UPDATE product
               SET stock = :stock, updated_at = now()
             WHERE id = :id
            RETURNING id, branch_id, name, stock
            """)
    Mono<ProductData> updateStock(UUID id, int stock);
}
