package com.nequi.franchise.r2dbc.commons;

import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.function.Function;

import static net.logstash.logback.argument.StructuredArguments.kv;

@UtilityClass
public class DatabaseLogs {

    public static final String REQUEST_KEY = "request";
    public static final String RESPONSE_KEY = "response";
    public static final String ERROR_KEY = "error";

    private static final String REQUEST = " [REQUEST]";
    private static final String RESPONSE = " [RESPONSE]";
    private static final String ERROR = " [ERROR]";

    public static <T> Function<Mono<T>, Mono<T>> logMono(Logger log, String operation, Object request) {
        return mono -> mono
                .doOnSubscribe(subscription -> log.info(operation + REQUEST, kv(REQUEST_KEY, request)))
                .doOnSuccess(response -> log.info(operation + RESPONSE, kv(RESPONSE_KEY, response)))
                .doOnError(error -> log.error(operation + ERROR, kv(ERROR_KEY, error.toString())));
    }

    public static <T> Function<Flux<T>, Flux<T>> logFlux(Logger log, String operation, Object request) {
        return flux -> flux
                .doOnSubscribe(subscription -> log.info(operation + REQUEST, kv(REQUEST_KEY, request)))
                .doOnComplete(() -> log.info(operation + RESPONSE))
                .doOnError(error -> log.error(operation + ERROR, kv(ERROR_KEY, error.toString())));
    }
}
