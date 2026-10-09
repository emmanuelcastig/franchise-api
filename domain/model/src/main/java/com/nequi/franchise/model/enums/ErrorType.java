package com.nequi.franchise.model.enums;

/**
 * Transport-agnostic error category. Entry points translate it to their own protocol (e.g. HTTP status).
 */
public enum ErrorType {
    INVALID_INPUT,
    NOT_FOUND,
    CONFLICT,
    UNAVAILABLE,
    UNEXPECTED
}
