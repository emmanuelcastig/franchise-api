package com.nequi.franchise.usecase.branch;

import com.nequi.franchise.model.branch.Branch;
import com.nequi.franchise.model.branch.gateways.BranchRepository;
import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.franchise.Franchise;
import com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static com.nequi.franchise.usecase.DomainErrorAssertions.isBusinessError;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BranchUseCaseTest {

    private static final UUID FRANCHISE_ID = UUID.randomUUID();
    private static final UUID BRANCH_ID = UUID.randomUUID();

    @Mock
    private FranchiseRepository franchiseRepository;
    @Mock
    private BranchRepository branchRepository;

    private BranchUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new BranchUseCase(franchiseRepository, branchRepository);
    }

    @Test
    @DisplayName("addBranch links the new branch to the franchise")
    void addBranchStoresBranch() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger Town")));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "Downtown")).thenReturn(Mono.empty());
        when(branchRepository.save(any())).thenAnswer(invocation -> Mono.just(
                invocation.<Branch>getArgument(0).toBuilder().id(BRANCH_ID).build()));

        StepVerifier.create(useCase.addBranch(FRANCHISE_ID, " Downtown "))
                .assertNext(branch -> {
                    assertThat(branch.id()).isEqualTo(BRANCH_ID);
                    assertThat(branch.franchiseId()).isEqualTo(FRANCHISE_ID);
                    assertThat(branch.name()).isEqualTo("Downtown");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("addBranch fails with not found when the franchise does not exist")
    void addBranchFailsWhenFranchiseIsMissing() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.addBranch(FRANCHISE_ID, "Downtown"))
                .expectErrorMatches(isBusinessError(TechnicalMessage.FRANCHISE_NOT_FOUND))
                .verify();

        verifyNoInteractions(branchRepository);
    }

    @Test
    @DisplayName("addBranch fails with conflict when the franchise already has a branch with that name")
    void addBranchRejectsDuplicatedName() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger Town")));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "Downtown"))
                .thenReturn(Mono.just(new Branch(UUID.randomUUID(), FRANCHISE_ID, "Downtown")));

        StepVerifier.create(useCase.addBranch(FRANCHISE_ID, "Downtown"))
                .expectErrorMatches(isBusinessError(TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS))
                .verify();

        verify(branchRepository, never()).save(any());
    }

    @Test
    @DisplayName("addBranch rejects an invalid name before any lookup")
    void addBranchRejectsInvalidName() {
        StepVerifier.create(useCase.addBranch(FRANCHISE_ID, ""))
                .expectErrorMatches(isBusinessError(TechnicalMessage.INVALID_NAME))
                .verify();

        verifyNoInteractions(franchiseRepository, branchRepository);
    }

    @Test
    @DisplayName("rename updates the branch name")
    void renameUpdatesName() {
        Branch renamed = new Branch(BRANCH_ID, FRANCHISE_ID, "Uptown");
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID))
                .thenReturn(Mono.just(new Branch(BRANCH_ID, FRANCHISE_ID, "Downtown")));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "Uptown")).thenReturn(Mono.empty());
        when(branchRepository.updateName(BRANCH_ID, "Uptown")).thenReturn(Mono.just(renamed));

        StepVerifier.create(useCase.rename(FRANCHISE_ID, BRANCH_ID, "Uptown"))
                .expectNext(renamed)
                .verifyComplete();
    }

    @Test
    @DisplayName("rename fails with not found when the branch belongs to another franchise")
    void renameFailsWhenBranchIsNotInFranchise() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.rename(FRANCHISE_ID, BRANCH_ID, "Uptown"))
                .expectErrorMatches(isBusinessError(TechnicalMessage.BRANCH_NOT_FOUND))
                .verify();

        verify(branchRepository, never()).updateName(any(), anyString());
    }

    @Test
    @DisplayName("rename fails with conflict when a sibling branch owns the new name")
    void renameRejectsNameOwnedBySibling() {
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID))
                .thenReturn(Mono.just(new Branch(BRANCH_ID, FRANCHISE_ID, "Downtown")));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "Uptown"))
                .thenReturn(Mono.just(new Branch(UUID.randomUUID(), FRANCHISE_ID, "Uptown")));

        StepVerifier.create(useCase.rename(FRANCHISE_ID, BRANCH_ID, "Uptown"))
                .expectErrorMatches(isBusinessError(TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS))
                .verify();
    }

    @Test
    @DisplayName("rename to its own current name is allowed")
    void renameToSameNameIsIdempotent() {
        Branch current = new Branch(BRANCH_ID, FRANCHISE_ID, "Downtown");
        when(branchRepository.findByIdAndFranchiseId(BRANCH_ID, FRANCHISE_ID)).thenReturn(Mono.just(current));
        when(branchRepository.findByFranchiseIdAndName(FRANCHISE_ID, "Downtown")).thenReturn(Mono.just(current));
        when(branchRepository.updateName(BRANCH_ID, "Downtown")).thenReturn(Mono.just(current));

        StepVerifier.create(useCase.rename(FRANCHISE_ID, BRANCH_ID, "Downtown"))
                .expectNext(current)
                .verifyComplete();
    }
}
