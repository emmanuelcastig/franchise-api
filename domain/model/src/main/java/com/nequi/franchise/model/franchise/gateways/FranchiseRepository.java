package com.nequi.franchise.model.franchise.gateways;

import com.nequi.franchise.model.franchise.Franchise;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FranchiseRepository {

    Mono<Franchise> save(Franchise franchise);

    Mono<Franchise> findById(UUID id);

    Mono<Franchise> findByName(String name);

    Mono<Franchise> updateName(UUID id, String name);
}
