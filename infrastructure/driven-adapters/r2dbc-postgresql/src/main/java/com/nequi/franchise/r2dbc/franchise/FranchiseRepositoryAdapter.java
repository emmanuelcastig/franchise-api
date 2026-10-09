package com.nequi.franchise.r2dbc.franchise;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.franchise.Franchise;
import com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import com.nequi.franchise.r2dbc.commons.DatabaseErrors;
import com.nequi.franchise.resilience.ResiliencePolicy;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
@RequiredArgsConstructor
public class FranchiseRepositoryAdapter implements FranchiseRepository {

    private final FranchiseDataRepository repository;
    private final ResiliencePolicy resilience;

    @Override
    public Mono<Franchise> save(Franchise franchise) {
        return resilience.nonIdempotent(repository.save(FranchiseData.fromDomain(franchise)))
                .map(FranchiseData::toDomain)
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS));
    }

    @Override
    public Mono<Franchise> findById(UUID id) {
        return resilience.idempotent(repository.findById(id))
                .map(FranchiseData::toDomain)
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Franchise> findByName(String name) {
        return resilience.idempotent(repository.findByName(name))
                .map(FranchiseData::toDomain)
                .onErrorMap(DatabaseErrors::translate);
    }

    @Override
    public Mono<Franchise> updateName(UUID id, String name) {
        return resilience.idempotent(repository.updateName(id, name))
                .map(FranchiseData::toDomain)
                .onErrorMap(DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS));
    }
}
