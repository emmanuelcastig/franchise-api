package com.nequi.franchise.model.product.gateways;

import com.nequi.franchise.model.product.BranchProductStock;
import com.nequi.franchise.model.product.Product;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ProductRepository {

    Mono<Product> save(Product product);

    Mono<Product> findByIdAndBranchId(UUID id, UUID branchId);

    Mono<Product> findByBranchIdAndName(UUID branchId, String name);

    Mono<Product> updateName(UUID id, String name);

    Mono<Product> updateStock(UUID id, int stock);

    Mono<Void> deleteById(UUID id);

    Flux<BranchProductStock> findStocksGroupedByBranch(UUID franchiseId);
}
