package com.nequi.franchise.model.franchise.gateways;

import com.nequi.franchise.model.franchise.Franchise;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FranchiseRepository {

    Mono<Franchise> save(Franchise franchise);

    /**
     * @return the franchise, or an empty Mono when it does not exist.
     */
    Mono<Franchise> findById(UUID id);

    /**
     * @return the franchise that owns the given name, or an empty Mono when the name is free.
     */
    Mono<Franchise> findByName(String name);

    /**
     * Updates only the name column, so concurrent changes to other attributes are never overwritten.
     *
     * @return the updated franchise, or an empty Mono when it does not exist.
     */
    Mono<Franchise> updateName(UUID id, String name);
}
