package com.nequi.franchise.api.handler;

import com.nequi.franchise.api.dto.request.NameRequest;
import com.nequi.franchise.api.dto.response.BranchResponse;
import com.nequi.franchise.api.validation.RequestValidator;
import com.nequi.franchise.usecase.branch.BranchUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.nequi.franchise.api.ApiPaths.BRANCH_ID;
import static com.nequi.franchise.api.ApiPaths.FRANCHISE_ID;
import static reactor.function.TupleUtils.function;

@Component
@RequiredArgsConstructor
public class BranchHandler {

    private final BranchUseCase branchUseCase;
    private final RequestValidator validator;

    public Mono<ServerResponse> addBranch(ServerRequest request) {
        return Mono.zip(validator.uuid(request, FRANCHISE_ID), validator.body(request, NameRequest.class))
                .flatMap(function((franchiseId, body) -> branchUseCase.addBranch(franchiseId, body.name())))
                .map(BranchResponse::fromDomain)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> renameBranch(ServerRequest request) {
        return Mono.zip(
                        validator.uuid(request, FRANCHISE_ID),
                        validator.uuid(request, BRANCH_ID),
                        validator.body(request, NameRequest.class))
                .flatMap(function((franchiseId, branchId, body) ->
                        branchUseCase.rename(franchiseId, branchId, body.name())))
                .map(BranchResponse::fromDomain)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}
