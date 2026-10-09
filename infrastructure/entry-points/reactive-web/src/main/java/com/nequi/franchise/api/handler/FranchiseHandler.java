package com.nequi.franchise.api.handler;

import com.nequi.franchise.api.dto.request.NameRequest;
import com.nequi.franchise.api.dto.response.FranchiseResponse;
import com.nequi.franchise.api.dto.response.TopStockProductResponse;
import com.nequi.franchise.api.validation.RequestValidator;
import com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import com.nequi.franchise.usecase.topstockproduct.TopStockProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.nequi.franchise.api.ApiPaths.FRANCHISE_ID;
import static reactor.function.TupleUtils.function;

@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final FranchiseUseCase franchiseUseCase;
    private final TopStockProductUseCase topStockProductUseCase;
    private final RequestValidator validator;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return validator.body(request, NameRequest.class)
                .flatMap(body -> franchiseUseCase.create(body.name()))
                .map(FranchiseResponse::fromDomain)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> renameFranchise(ServerRequest request) {
        return Mono.zip(validator.uuid(request, FRANCHISE_ID), validator.body(request, NameRequest.class))
                .flatMap(function((franchiseId, body) -> franchiseUseCase.rename(franchiseId, body.name())))
                .map(FranchiseResponse::fromDomain)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    /**
     * The aggregation streams rows from the database with backpressure; only the result (one entry per branch)
     * is collected, so errors such as FRANCHISE_NOT_FOUND surface before the response status is committed.
     */
    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        return validator.uuid(request, FRANCHISE_ID)
                .flatMapMany(topStockProductUseCase::findTopStockProductPerBranch)
                .map(TopStockProductResponse::fromDomain)
                .collectList()
                .flatMap(products -> ServerResponse.ok().bodyValue(products));
    }
}
