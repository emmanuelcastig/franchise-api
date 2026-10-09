package com.nequi.franchise.r2dbc.commons;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.exceptions.DomainException;
import com.nequi.franchise.model.exceptions.TechnicalException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import lombok.experimental.UtilityClass;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.TransientDataAccessException;

import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Anti-corruption layer for persistence errors: nothing from Spring Data, R2DBC or Resilience4j leaks into the
 * domain. Applied with {@code onErrorMap}, after the resilience operators have seen the original exception.
 */
@UtilityClass
public class DatabaseErrors {

    public static Throwable translate(Throwable error) {
        return switch (error) {
            case DomainException domainException -> domainException;
            case CallNotPermittedException _, TimeoutException _, TransientDataAccessException _,
                 DataAccessResourceFailureException _ -> new TechnicalException(TechnicalMessage.DATABASE_UNAVAILABLE, error);
            default -> new TechnicalException(TechnicalMessage.UNEXPECTED_ERROR, error);
        };
    }

    /**
     * A unique constraint is the race-free guarantee behind the "unique name" rule: if two requests pass the
     * use case pre-check at the same time, the database rejects the second insert or rename.
     */
    public static Function<Throwable, Throwable> translateDuplicateNameTo(TechnicalMessage duplicateNameMessage) {
        return error -> switch (error) {
            case DuplicateKeyException _ -> new BusinessException(duplicateNameMessage);
            default -> translate(error);
        };
    }
}
