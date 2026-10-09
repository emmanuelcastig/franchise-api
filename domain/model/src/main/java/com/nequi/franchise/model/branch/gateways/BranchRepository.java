package com.nequi.franchise.model.branch.gateways;

import com.nequi.franchise.model.branch.Branch;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface BranchRepository {

    Mono<Branch> save(Branch branch);

    Mono<Branch> findByIdAndFranchiseId(UUID id, UUID franchiseId);

    Mono<Branch> findByFranchiseIdAndName(UUID franchiseId, String name);

    Mono<Branch> updateName(UUID id, String name);
}
