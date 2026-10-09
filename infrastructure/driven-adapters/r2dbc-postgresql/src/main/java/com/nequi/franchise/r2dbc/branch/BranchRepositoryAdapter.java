package com.nequi.franchise.r2dbc.branch;

import com.nequi.franchise.model.branch.Branch;
import com.nequi.franchise.model.branch.gateways.BranchRepository;
import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.r2dbc.commons.DatabaseErrors;
import com.nequi.franchise.resilience.ResiliencePolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.UUID;

import static com.nequi.franchise.r2dbc.commons.DatabaseLogs.logMono;

@Slf4j
@Repository
@RequiredArgsConstructor
public class BranchRepositoryAdapter implements BranchRepository {

    private static final String SAVE = "Save Branch";
    private static final String FIND_BY_ID_AND_FRANCHISE = "Find Branch By Id And Franchise";
    private static final String FIND_BY_FRANCHISE_AND_NAME = "Find Branch By Franchise And Name";
    private static final String UPDATE_NAME = "Update Branch Name";

    private final BranchDataRepository repository;
    private final ResiliencePolicy resilience;

    @Override
    public Mono<Branch> save(Branch branch) {
        return resilience.nonIdempotent(repository.save(BranchData.fromDomain(branch)))
                .map(BranchData::toDomain)
                .transform(logMono(log, SAVE, branch))
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Branch> findByIdAndFranchiseId(UUID id, UUID franchiseId) {
        return resilience.idempotent(repository.findByIdAndFranchiseId(id, franchiseId))
                .map(BranchData::toDomain)
                .transform(logMono(log, FIND_BY_ID_AND_FRANCHISE, Map.of("id", id, "franchiseId", franchiseId)))
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Branch> findByFranchiseIdAndName(UUID franchiseId, String name) {
        return resilience.idempotent(repository.findByFranchiseIdAndName(franchiseId, name))
                .map(BranchData::toDomain)
                .transform(logMono(log, FIND_BY_FRANCHISE_AND_NAME, Map.of("franchiseId", franchiseId, "name", name)))
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Branch> updateName(UUID id, String name) {
        return resilience.idempotent(repository.updateName(id, name))
                .map(BranchData::toDomain)
                .transform(logMono(log, UPDATE_NAME, Map.of("id", id, "name", name)))
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS));
    }
}
