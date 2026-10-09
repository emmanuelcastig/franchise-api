package com.nequi.franchise.api.validation;

import com.nequi.franchise.model.enums.TechnicalMessage;
import lombok.Getter;

import java.util.List;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final transient List<FieldViolation> violations;

    public InvalidRequestException(List<FieldViolation> violations) {
        super(TechnicalMessage.INVALID_REQUEST.getMessage());
        this.violations = List.copyOf(violations);
    }

    public InvalidRequestException(String field, String message) {
        this(List.of(new FieldViolation(field, message)));
    }
}
