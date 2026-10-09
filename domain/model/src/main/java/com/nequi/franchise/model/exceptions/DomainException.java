package com.nequi.franchise.model.exceptions;

import com.nequi.franchise.model.enums.TechnicalMessage;
import lombok.Getter;

/**
 * Root of the domain error hierarchy. It is sealed so error translators can switch over it exhaustively.
 */
@Getter
public abstract sealed class DomainException extends RuntimeException permits BusinessException, TechnicalException {

    private final TechnicalMessage technicalMessage;

    protected DomainException(TechnicalMessage technicalMessage, Throwable cause) {
        super(technicalMessage.getMessage(), cause);
        this.technicalMessage = technicalMessage;
    }
}
