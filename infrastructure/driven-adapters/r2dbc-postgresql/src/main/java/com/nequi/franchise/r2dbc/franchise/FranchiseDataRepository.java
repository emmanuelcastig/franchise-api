package com.nequi.franchise.r2dbc.franchise;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface FranchiseDataRepository extends ReactiveCrudRepository<FranchiseData, UUID> {

    Mono<FranchiseData> findByName(String name);

    @Query("""
            UPDATE franchise
               SET name = :name, updated_at = now()
             WHERE id = :id
            RETURNING id, name
            """)
    Mono<FranchiseData> updateName(UUID id, String name);
}
