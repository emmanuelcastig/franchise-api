package com.nequi.franchise.r2dbc.franchise;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.franchise.Franchise;
import com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
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
public class FranchiseRepositoryAdapter implements FranchiseRepository {

    private static final String SAVE = "Save Franchise";
    private static final String FIND_BY_ID = "Find Franchise By Id";
    private static final String FIND_BY_NAME = "Find Franchise By Name";
    private static final String UPDATE_NAME = "Update Franchise Name";

    private final FranchiseDataRepository repository;
    private final ResiliencePolicy resilience;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return resilience.nonIdempotent(repository.save(FranchiseData.fromDomain(franchise)))
                .map(FranchiseData::toDomain)
                .transform(logMono(log, SAVE, franchise))
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Franchise> findById(UUID id) {
        return resilience.idempotent(repository.findById(id))
                .map(FranchiseData::toDomain)
                .transform(logMono(log, FIND_BY_ID, id))
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Franchise> findByName(String name) {
        return resilience.idempotent(repository.findByName(name))
                .map(FranchiseData::toDomain)
                .transform(logMono(log, FIND_BY_NAME, name))
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Franchise> updateName(UUID id, String name) {
        return resilience.idempotent(repository.updateName(id, name))
                .map(FranchiseData::toDomain)
                .transform(logMono(log, UPDATE_NAME, Map.of("id", id, "name", name)))
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS));
    }
}
