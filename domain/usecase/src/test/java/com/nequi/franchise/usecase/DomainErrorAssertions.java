package com.nequi.franchise.usecase;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;

import java.util.function.Predicate;

public final class DomainErrorAssertions {

    private DomainErrorAssertions() {
    }

    public static Predicate<Throwable> isBusinessError(TechnicalMessage expected) {
        return error -> error instanceof BusinessException businessException
                && businessException.getTechnicalMessage() == expected;
    }
}
