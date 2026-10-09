package com.nequi.franchise.r2dbc.branch;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BranchDataRepository extends ReactiveCrudRepository<BranchData, UUID> {

    Mono<BranchData> findByIdAndFranchiseId(UUID id, UUID franchiseId);

    Mono<BranchData> findByFranchiseIdAndName(UUID franchiseId, String name);

    @Query("""
            UPDATE branch
               SET name = :name, updated_at = now()
             WHERE id = :id
            RETURNING id, franchise_id, name
            """)
    Mono<BranchData> updateName(UUID id, String name);
}
