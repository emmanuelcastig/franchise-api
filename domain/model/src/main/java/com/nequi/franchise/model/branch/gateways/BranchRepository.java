package com.nequi.franchise.model.branch.gateways;

import com.nequi.franchise.model.branch.Branch;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BranchRepository {

    Mono<Branch> save(Branch branch);

    /**
     * @return the branch when it exists and belongs to the franchise, otherwise an empty Mono.
     */
    Mono<Branch> findByIdAndFranchiseId(UUID id, UUID franchiseId);

    /**
     * @return the branch of the franchise that owns the given name, or an empty Mono when the name is free.
     */
    Mono<Branch> findByFranchiseIdAndName(UUID franchiseId, String name);

    /**
     * Updates only the name column.
     *
     * @return the updated branch, or an empty Mono when it does not exist.
     */
    Mono<Branch> updateName(UUID id, String name);
}
