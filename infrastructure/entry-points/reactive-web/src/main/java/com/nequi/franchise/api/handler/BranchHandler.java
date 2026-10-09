package com.nequi.franchise.api.handler;

import com.nequi.franchise.api.dto.request.NameRequest;
import com.nequi.franchise.api.dto.response.BranchResponse;
import com.nequi.franchise.api.utils.Constants.LogKeys;
import com.nequi.franchise.api.utils.Constants.LogMessages.AddBranch;
import com.nequi.franchise.api.utils.Constants.LogMessages.RenameBranch;
import com.nequi.franchise.api.validation.RequestValidator;
import com.nequi.franchise.usecase.branch.BranchUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.nequi.franchise.api.ApiPaths.BRANCH_ID;
import static com.nequi.franchise.api.ApiPaths.FRANCHISE_ID;
import static net.logstash.logback.argument.StructuredArguments.kv;
import static reactor.function.TupleUtils.consumer;
import static reactor.function.TupleUtils.function;

@Slf4j
@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final BranchUseCase branchUseCase;
    private final RequestValidator validator;

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        return Mono.zip(validator.uuid(request, FRANCHISE_ID), validator.body(request, NameRequest.class))
                .doOnNext(consumer((franchiseId, body) -> log.info(AddBranch.REQUEST,
                        kv(FRANCHISE_ID, franchiseId), kv(LogKeys.REQUEST, body))))
                .flatMap(function((franchiseId, body) -> branchUseCase.addBranch(franchiseId, body.name())))
                .map(BranchResponse::fromDomain)
                .doOnNext(response -> log.info(AddBranch.RESPONSE, kv(LogKeys.RESPONSE, response)))
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> renameBranch(ServerRequest request) {
        return Mono.zip(
                        validator.uuid(request, FRANCHISE_ID),
                        validator.uuid(request, BRANCH_ID),
                        validator.body(request, NameRequest.class))
                .doOnNext(consumer((franchiseId, branchId, body) -> log.info(RenameBranch.REQUEST,
                        kv(FRANCHISE_ID, franchiseId), kv(BRANCH_ID, branchId), kv(LogKeys.REQUEST, body))))
                .flatMap(function((franchiseId, branchId, body) ->
                        branchUseCase.rename(franchiseId, branchId, body.name())))
                .map(BranchResponse::fromDomain)
                .doOnNext(response -> log.info(RenameBranch.RESPONSE, kv(LogKeys.RESPONSE, response)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}
