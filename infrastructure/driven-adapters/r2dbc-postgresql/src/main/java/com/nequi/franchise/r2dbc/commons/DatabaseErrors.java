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

    public static Function<Throwable, Throwable> translateDuplicateNameTo(TechnicalMessage duplicateNameMessage) {
        return error -> switch (error) {
            case DuplicateKeyException _ -> new BusinessException(duplicateNameMessage);
            default -> translate(error);
        };
    }
}
