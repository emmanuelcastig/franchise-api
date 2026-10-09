package com.nequi.franchise.model.exceptions;

import com.nequi.franchise.model.enums.TechnicalMessage;
import lombok.Getter;

@Getter
public abstract sealed class DomainException extends RuntimeException permits BusinessException, TechnicalException {

    private final TechnicalMessage technicalMessage;

    protected DomainException(TechnicalMessage technicalMessage, Throwable cause) {
        super(technicalMessage.getMessage(), cause);
        this.technicalMessage = technicalMessage;
    }
}
