package com.nequi.franchise.model.exceptions;

import com.nequi.franchise.model.enums.TechnicalMessage;

/**
 * A business rule was violated. It is expected, so it must not trip circuit breakers or be retried.
 */
public final class BusinessException extends DomainException {

    public BusinessException(TechnicalMessage technicalMessage) {
        super(technicalMessage, null);
    }
}
