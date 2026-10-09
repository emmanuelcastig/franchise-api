package com.nequi.franchise.model.exceptions;

import com.nequi.franchise.model.enums.TechnicalMessage;

public final class TechnicalException extends DomainException {

    public TechnicalException(TechnicalMessage technicalMessage, Throwable cause) {
        super(technicalMessage, cause);
    }
}
