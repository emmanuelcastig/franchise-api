package com.nequi.franchise.r2dbc.commons;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.exceptions.DomainException;
import com.nequi.franchise.model.exceptions.TechnicalException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.QueryTimeoutException;

import java.util.concurrent.TimeoutException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DatabaseErrorsTest {

    static List<Throwable> unavailabilityErrors() {
        return List.of(
                CallNotPermittedException.createCallNotPermittedException(CircuitBreaker.ofDefaults("database")),
                new TimeoutException("time limiter"),
                new QueryTimeoutException("slow query"),
                new DataAccessResourceFailureException("connection refused"));
    }

    @ParameterizedTest
    @MethodSource("unavailabilityErrors")
    @DisplayName("infrastructure unavailability becomes DATABASE_UNAVAILABLE keeping the cause")
    void translatesUnavailability(Throwable error) {
        Throwable translated = DatabaseErrors.translate(error);

        assertThat(translated).isInstanceOf(TechnicalException.class).hasCause(error);
        assertThat(((DomainException) translated).getTechnicalMessage()).isEqualTo(TechnicalMessage.DATABASE_UNAVAILABLE);
    }

    @Test
    @DisplayName("unknown errors become UNEXPECTED_ERROR")
    void translatesUnknownErrors() {
        Throwable translated = DatabaseErrors.translate(new DataIntegrityViolationException("check constraint"));

        assertThat(((DomainException) translated).getTechnicalMessage()).isEqualTo(TechnicalMessage.UNEXPECTED_ERROR);
    }

    @Test
    @DisplayName("domain exceptions pass through untouched")
    void keepsDomainExceptions() {
        BusinessException businessException = new BusinessException(TechnicalMessage.PRODUCT_NOT_FOUND);

        assertThat(DatabaseErrors.translate(businessException)).isSameAs(businessException);
    }

    @Test
    @DisplayName("a unique constraint violation becomes the given business conflict")
    void translatesDuplicateKeys() {
        Throwable translated = DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS)
                .apply(new DuplicateKeyException("uk_branch_franchise_name"));

        assertThat(translated).isInstanceOf(BusinessException.class);
        assertThat(((DomainException) translated).getTechnicalMessage()).isEqualTo(TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("the duplicate translator falls back to the generic translation for other errors")
    void duplicateTranslatorFallsBack() {
        Throwable translated = DatabaseErrors.translateDuplicateNameTo(TechnicalMessage.BRANCH_NAME_ALREADY_EXISTS)
                .apply(new TimeoutException());

        assertThat(((DomainException) translated).getTechnicalMessage()).isEqualTo(TechnicalMessage.DATABASE_UNAVAILABLE);
    }
}
