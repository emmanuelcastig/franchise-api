package com.nequi.franchise.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TechnicalMessage {

    INVALID_REQUEST("FRN-400", "The request is invalid, please verify the data", ErrorType.INVALID_INPUT),
    INVALID_NAME("FRN-401", "The name must not be blank and must have at most 100 characters", ErrorType.INVALID_INPUT),
    INVALID_STOCK("FRN-402", "The stock must be zero or greater", ErrorType.INVALID_INPUT),

    FRANCHISE_NOT_FOUND("FRN-404", "The franchise does not exist", ErrorType.NOT_FOUND),
    BRANCH_NOT_FOUND("FRN-405", "The branch does not exist in the given franchise", ErrorType.NOT_FOUND),
    PRODUCT_NOT_FOUND("FRN-406", "The product does not exist in the given branch", ErrorType.NOT_FOUND),

    FRANCHISE_NAME_ALREADY_EXISTS("FRN-409", "A franchise with the same name already exists", ErrorType.CONFLICT),
    BRANCH_NAME_ALREADY_EXISTS("FRN-410", "A branch with the same name already exists in the franchise", ErrorType.CONFLICT),
    PRODUCT_NAME_ALREADY_EXISTS("FRN-411", "A product with the same name already exists in the branch", ErrorType.CONFLICT),

    DATABASE_UNAVAILABLE("FRN-503", "The service is temporarily unavailable, please try again later", ErrorType.UNAVAILABLE),
    UNEXPECTED_ERROR("FRN-500", "Something went wrong, please try again later", ErrorType.UNEXPECTED);

    private final String code;
    private final String message;
    private final ErrorType type;
}
