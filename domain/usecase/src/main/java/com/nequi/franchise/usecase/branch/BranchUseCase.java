package com.nequi.franchise.usecase.branch;

import com.nequi.franchise.model.branch.Branch;
import com.nequi.franchise.model.branch.gateways.BranchRepository;
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
public class BranchUseCase {

    private final FranchiseRepository franchiseRepository;
    private final BranchRepository branchRepository;

    public Mono<Branch> addBranch(UUID franchiseId, String name) {
        return validName(name)
                .flatMap(validName -> findFranchise(franchiseId).thenReturn(validName))
                .flatMap(validName -> rejectIfPresent(branchRepository.findByFranchiseIdAndName(franchiseId, validName),
                        TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS)
                        .thenReturn(validName))
                .map(validName -> Branch.builder().franchiseId(franchiseId).name(validName).build())
                .flatMap(branchRepository::save);
    }

    public Mono<Branch> rename(UUID franchiseId, UUID branchId, String newName) {
        return validName(newName)
                .flatMap(validName -> findBranch(franchiseId, branchId).thenReturn(validName))
                .flatMap(validName -> rejectIfPresent(branchRepository.findByFranchiseIdAndName(franchiseId, validName)
                                .filter(owner -> !owner.id().equals(branchId)),
                        TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS)
                        .thenReturn(validName))
                .flatMap(validName -> branchRepository.updateName(branchId, validName))
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.BRANCH_NOT_FOUND)));
    }

    private Mono<Franchise> findFranchise(UUID franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)));
    }

    private Mono<Branch> findBranch(UUID franchiseId, UUID branchId) {
        return branchRepository.findByIdAndFranchiseId(branchId, franchiseId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.BRANCH_NOT_FOUND)));
    }
}
