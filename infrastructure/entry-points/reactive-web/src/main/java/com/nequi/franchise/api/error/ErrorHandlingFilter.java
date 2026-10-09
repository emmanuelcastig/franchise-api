package com.nequi.franchise.api.error;

import com.nequi.franchise.api.validation.FieldViolation;
import com.nequi.franchise.api.validation.InvalidRequestException;
import com.nequi.franchise.model.enums.ErrorType;
import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.exceptions.DomainException;
import com.nequi.franchise.model.exceptions.TechnicalException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.HandlerFilterFunction;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.time.Instant;
import java.util.List;

/**
 * Functional counterpart of an aspect around every handler: it translates error signals into RFC 9457
 * problem responses with onErrorResume, so handlers only describe the happy path.
 */
@Slf4j
@Component
public class ErrorHandlingFilter implements HandlerFilterFunction<ServerResponse, ServerResponse> {

    private static final String CODE = "code";
    private static final String TIMESTAMP = "timestamp";
    private static final String ERRORS = "errors";

    @Override
    public Mono<ServerResponse> filter(ServerRequest request, HandlerFunction<ServerResponse> next) {
        return next.handle(request)
                .onErrorResume(DomainException.class, error -> domainError(request, error))
                .onErrorResume(InvalidRequestException.class, error -> invalidRequest(request, error))
                .onErrorResume(ResponseStatusException.class, error -> frameworkError(request, error))
                .onErrorResume(error -> unexpectedError(request, error));
    }

    private Mono<ServerResponse> domainError(ServerRequest request, DomainException error) {
        logDomainError(request, error);
        TechnicalMessage message = error.getTechnicalMessage();
        return respond(problem(request, toHttpStatus(message.getType()), message.getCode(), message.getMessage()));
    }

    private Mono<ServerResponse> invalidRequest(ServerRequest request, InvalidRequestException error) {
        log.info("Invalid request {} {}: {}", request.method(), request.path(), error.getViolations());
        ProblemDetail problem = problem(request, HttpStatus.BAD_REQUEST,
                TechnicalMessage.INVALID_REQUEST.getCode(), TechnicalMessage.INVALID_REQUEST.getMessage());
        problem.setProperty(ERRORS, error.getViolations());
        return respond(problem);
    }

    /**
     * Framework rejections such as a malformed JSON body (ServerWebInputException) or an unsupported media type.
     */
    private Mono<ServerResponse> frameworkError(ServerRequest request, ResponseStatusException error) {
        log.info("Request rejected {} {}: {}", request.method(), request.path(), error.getMessage());
        ProblemDetail problem = problem(request, error.getStatusCode(),
                TechnicalMessage.INVALID_REQUEST.getCode(), TechnicalMessage.INVALID_REQUEST.getMessage());
        problem.setProperty(ERRORS, List.of(new FieldViolation("body", error.getReason())));
        return respond(problem);
    }

    private Mono<ServerResponse> unexpectedError(ServerRequest request, Throwable error) {
        log.error("Unexpected error {} {}", request.method(), request.path(), error);
        TechnicalMessage message = TechnicalMessage.UNEXPECTED_ERROR;
        return respond(problem(request, HttpStatus.INTERNAL_SERVER_ERROR, message.getCode(), message.getMessage()));
    }

    private static void logDomainError(ServerRequest request, DomainException error) {
        switch (error) {
            case BusinessException business -> log.info("Business rule rejected {} {}: {}",
                    request.method(), request.path(), business.getTechnicalMessage().getCode());
            case TechnicalException technical -> log.error("Technical failure on {} {}: {}",
                    request.method(), request.path(), technical.getTechnicalMessage().getCode(), technical);
        }
    }

    private static HttpStatus toHttpStatus(ErrorType type) {
        return switch (type) {
            case INVALID_INPUT -> HttpStatus.BAD_REQUEST;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case UNEXPECTED -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private static ProblemDetail problem(ServerRequest request, HttpStatusCode status, String code, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setInstance(URI.create(request.path()));
        problem.setProperty(CODE, code);
        problem.setProperty(TIMESTAMP, Instant.now());
        return problem;
    }

    private static Mono<ServerResponse> respond(ProblemDetail problem) {
        return ServerResponse.status(problem.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .bodyValue(problem);
    }
}
