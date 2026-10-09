package com.nequi.franchise.model.exceptions;

import com.nequi.franchise.model.enums.TechnicalMessage;

public final class BusinessException extends DomainException {

    public BusinessException(TechnicalMessage technicalMessage) {
        super(technicalMessage, null);
    }
}
