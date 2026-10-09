package com.nequi.franchise.usecase.commons;

import com.nequi.franchise.model.commons.NameRules;
import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.product.Product;
import lombok.experimental.UtilityClass;
import reactor.core.publisher.Mono;

/**
 * Reactive guards shared by the use cases. Each one either emits the validated value or fails with a
 * {@link BusinessException}, so callers can chain them without imperative branching.
 */
@UtilityClass
public class Validations {

    public static Mono<String> validName(String rawName) {
        return Mono.justOrEmpty(rawName)
                .filter(NameRules::isValid)
                .map(String::strip)
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.INVALID_NAME)));
    }

    public static Mono<Integer> validStock(int stock) {
        return Mono.just(stock)
                .filter(Product::isValidStock)
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.INVALID_STOCK)));
    }

    /**
     * Fails with the given message when the lookup emits an element; completes empty otherwise.
     */
    public static <T> Mono<Void> rejectIfPresent(Mono<T> lookup, TechnicalMessage message) {
        return lookup
                .flatMap(found -> Mono.<Void>error(() -> new BusinessException(message)))
                .then();
    }
}
