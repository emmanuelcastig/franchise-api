package com.nequi.franchise.usecase.franchise;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.franchise.Franchise;
import com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FranchiseUseCaseTest {

    private static final UUID FRANCHISE_ID = UUID.randomUUID();

    @Mock
    private FranchiseRepository franchiseRepository;

    private FranchiseUseCase useCase;

    @BeforeEach
    void setUp() {
        useCase = new FranchiseUseCase(franchiseRepository);
    }

    @Test
    @DisplayName("create stores a franchise with the trimmed name when the name is free")
    void createStoresFranchise() {
        when(franchiseRepository.findByName("Burger Town")).thenReturn(Mono.empty());
        when(franchiseRepository.save(any())).thenAnswer(invocation -> Mono.just(
                invocation.<Franchise>getArgument(0).toBuilder().id(FRANCHISE_ID).build()));

        StepVerifier.create(useCase.create("  Burger Town "))
                .assertNext(franchise -> {
                    assertThat(franchise.id()).isEqualTo(FRANCHISE_ID);
                    assertThat(franchise.name()).isEqualTo("Burger Town");
                })
                .verifyComplete();

        ArgumentCaptor<Franchise> captor = ArgumentCaptor.forClass(Franchise.class);
        verify(franchiseRepository).save(captor.capture());
        assertThat(captor.getValue().id()).isNull();
    }

    @Test
    @DisplayName("create fails with conflict when another franchise already owns the name")
    void createRejectsDuplicatedName() {
        when(franchiseRepository.findByName("Burger Town"))
                .thenReturn(Mono.just(new Franchise(UUID.randomUUID(), "Burger Town")));

        StepVerifier.create(useCase.create("Burger Town"))
                .expectErrorMatches(isBusinessError(TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS))
                .verify();

        verify(franchiseRepository, never()).save(any());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   ", "\t"})
    @DisplayName("create rejects blank names without touching the repository")
    void createRejectsBlankName(String name) {
        StepVerifier.create(useCase.create(name))
                .expectErrorMatches(isBusinessError(TechnicalMessage.INVALID_NAME))
                .verify();

        verify(franchiseRepository, never()).findByName(anyString());
    }

    @Test
    @DisplayName("create rejects names longer than the allowed maximum")
    void createRejectsTooLongName() {
        StepVerifier.create(useCase.create("x".repeat(101)))
                .expectErrorMatches(isBusinessError(TechnicalMessage.INVALID_NAME))
                .verify();
    }

    @Test
    @DisplayName("rename updates the name when the franchise exists and the name is free")
    void renameUpdatesName() {
        Franchise renamed = new Franchise(FRANCHISE_ID, "Burger City");
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger Town")));
        when(franchiseRepository.findByName("Burger City")).thenReturn(Mono.empty());
        when(franchiseRepository.updateName(FRANCHISE_ID, "Burger City")).thenReturn(Mono.just(renamed));

        StepVerifier.create(useCase.rename(FRANCHISE_ID, "Burger City"))
                .expectNext(renamed)
                .verifyComplete();
    }

    @Test
    @DisplayName("rename to its own current name is allowed, which keeps the operation idempotent")
    void renameToSameNameIsIdempotent() {
        Franchise current = new Franchise(FRANCHISE_ID, "Burger Town");
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(current));
        when(franchiseRepository.findByName("Burger Town")).thenReturn(Mono.just(current));
        when(franchiseRepository.updateName(FRANCHISE_ID, "Burger Town")).thenReturn(Mono.just(current));

        StepVerifier.create(useCase.rename(FRANCHISE_ID, "Burger Town"))
                .expectNext(current)
                .verifyComplete();
    }

    @Test
    @DisplayName("rename fails with conflict when a different franchise owns the new name")
    void renameRejectsNameOwnedByAnotherFranchise() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger Town")));
        when(franchiseRepository.findByName("Pizza Town"))
                .thenReturn(Mono.just(new Franchise(UUID.randomUUID(), "Pizza Town")));

        StepVerifier.create(useCase.rename(FRANCHISE_ID, "Pizza Town"))
                .expectErrorMatches(isBusinessError(TechnicalMessage.FRANCHISE_NAME_ALREADY_EXISTS))
                .verify();

        verify(franchiseRepository, never()).updateName(any(), anyString());
    }

    @Test
    @DisplayName("rename fails with not found when the franchise does not exist")
    void renameFailsWhenFranchiseIsMissing() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.empty());

        StepVerifier.create(useCase.rename(FRANCHISE_ID, "Burger City"))
                .expectErrorMatches(isBusinessError(TechnicalMessage.FRANCHISE_NOT_FOUND))
                .verify();
    }

    @Test
    @DisplayName("rename fails with not found when the franchise disappears before the update")
    void renameFailsWhenUpdateFindsNothing() {
        when(franchiseRepository.findById(FRANCHISE_ID)).thenReturn(Mono.just(new Franchise(FRANCHISE_ID, "Burger Town")));
        when(franchiseRepository.findByName("Burger City")).thenReturn(Mono.empty());
        when(franchiseRepository.updateName(FRANCHISE_ID, "Burger City")).thenReturn(Mono.empty());

        StepVerifier.create(useCase.rename(FRANCHISE_ID, "Burger City"))
                .expectErrorMatches(isBusinessError(TechnicalMessage.FRANCHISE_NOT_FOUND))
                .verify();
    }

    @Test
    @DisplayName("repository failures are propagated untouched")
    void createPropagatesRepositoryErrors() {
        IllegalStateException failure = new IllegalStateException("boom");
        when(franchiseRepository.findByName("Burger Town")).thenReturn(Mono.error(failure));

        StepVerifier.create(useCase.create("Burger Town"))
                .expectErrorMatches(error -> error == failure && !(error instanceof BusinessException))
                .verify();
    }
}
