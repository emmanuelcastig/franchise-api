package com.nequi.franchise.api.validation;

import jakarta.annotation.PostConstruct;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@Component
@RequiredArgsConstructor
public class RequestValidator {

    private static final Predicate<String> CANONICAL_UUID = Pattern.compile(
            "^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$").asMatchPredicate();

    private final Validator validator;

    @PostConstruct
    void warmUp() {
        validator.validate(new WarmUpSample(null));
    }

    public <T> Mono<T> body(ServerRequest request, Class<T> type) {
        return request.bodyToMono(type)
                .switchIfEmpty(Mono.error(() -> new InvalidRequestException("body", "must not be empty")))
                .flatMap(this::validate);
    }

    public Mono<UUID> uuid(ServerRequest request, String pathVariable) {
        return Mono.just(request.pathVariable(pathVariable))
                .filter(CANONICAL_UUID)
                .map(UUID::fromString)
                .switchIfEmpty(Mono.error(() -> new InvalidRequestException(pathVariable, "must be a valid UUID")));
    }

    private <T> Mono<T> validate(T body) {
        return Mono.fromSupplier(() -> validator.validate(body))
                .flatMapMany(Flux::fromIterable)
                .map(violation -> new FieldViolation(violation.getPropertyPath().toString(), violation.getMessage()))
                .collectSortedList(Comparator.comparing(FieldViolation::field))
                .flatMap(violations -> Mono.just(body)
                        .filter(validBody -> violations.isEmpty())
                        .switchIfEmpty(Mono.error(() -> new InvalidRequestException(violations))));
    }

    private record WarmUpSample(@NotBlank String value) {
    }
}
