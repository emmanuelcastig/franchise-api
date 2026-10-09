package com.nequi.franchise.usecase.franchise;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.franchise.Franchise;
import com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.nequi.franchise.usecase.commons.Validations.rejectIfPresent;
import static com.nequi.franchise.usecase.commons.Validations.validName;

@RequiredArgsConstructor
public class FranchiseUseCase {

    private final FranchiseRepository franchiseRepository;

    public Mono<Franchise> create(String name) {
        return validName(name)
                .flatMap(validName -> rejectIfPresent(franchiseRepository.findByName(validName),
                        TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS)
                        .thenReturn(validName))
                .map(validName -> Franchise.builder().name(validName).build())
                .flatMap(franchiseRepository::save);
    }

    public Mono<Franchise> rename(UUID franchiseId, String newName) {
        return validName(newName)
                .flatMap(validName -> findFranchise(franchiseId).thenReturn(validName))
                .flatMap(validName -> rejectIfPresent(franchiseRepository.findByName(validName)
                                .filter(owner -> !owner.id().equals(franchiseId)),
                        TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS)
                        .thenReturn(validName))
                .flatMap(validName -> franchiseRepository.updateName(franchiseId, validName))
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)));
    }

    private Mono<Franchise> findFranchise(UUID franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)));
    }
}
