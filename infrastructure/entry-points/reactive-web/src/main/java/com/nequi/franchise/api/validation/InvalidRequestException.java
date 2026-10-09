package com.nequi.franchise.api.validation;

import com.nequi.franchise.model.enums.TechnicalMessage;
import lombok.Getter;

import java.util.List;

/**
 * The HTTP contract was not honoured (missing body, malformed identifier, constraint violations).
 * It belongs to the entry point: the domain never sees requests that fail here.
 */
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
