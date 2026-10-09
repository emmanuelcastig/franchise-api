package com.nequi.franchise.r2dbc.branch;

import com.nequi.franchise.model.branch.Branch;
import com.nequi.franchise.model.branch.gateways.BranchRepository;
import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.r2dbc.commons.DatabaseErrors;
import com.nequi.franchise.resilience.ResiliencePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class BranchRepositoryAdapter implements BranchRepository {

    private final BranchDataRepository repository;
    private final ResiliencePolicy resilience;

    @Override
    public Mono<Branch> save(Branch branch) {
        return resilience.nonIdempotent(repository.save(BranchData.fromDomain(branch)))
                .map(BranchData::toDomain)
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Branch> findByIdAndFranchiseId(UUID id, UUID franchiseId) {
        return resilience.idempotent(repository.findByIdAndFranchiseId(id, franchiseId))
                .map(BranchData::toDomain)
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Branch> findByFranchiseIdAndName(UUID franchiseId, String name) {
        return resilience.idempotent(repository.findByFranchiseIdAndName(franchiseId, name))
                .map(BranchData::toDomain)
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Branch> updateName(UUID id, String name) {
        return resilience.idempotent(repository.updateName(id, name))
                .map(BranchData::toDomain)
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS));
    }
}
