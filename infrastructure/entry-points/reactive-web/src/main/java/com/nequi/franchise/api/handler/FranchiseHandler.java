package com.nequi.franchise.api.handler;

import com.nequi.franchise.api.dto.request.NameRequest;
import com.nequi.franchise.api.dto.response.FranchiseResponse;
import com.nequi.franchise.api.dto.response.TopStockProductResponse;
import com.nequi.franchise.api.utils.Constants.LogKeys;
import com.nequi.franchise.api.utils.Constants.LogMessages.CreateFranchise;
import com.nequi.franchise.api.utils.Constants.LogMessages.RenameFranchise;
import com.nequi.franchise.api.utils.Constants.LogMessages.TopStockProducts;
import com.nequi.franchise.api.validation.RequestValidator;
import com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import com.nequi.franchise.usecase.topstockproduct.TopStockProductUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.nequi.franchise.api.ApiPaths.FRANCHISE_ID;
import static net.logstash.logback.argument.StructuredArguments.kv;
import static reactor.function.TupleUtils.consumer;
import static reactor.function.TupleUtils.function;

@Slf4j
@Component
@RequiredArgsConstructor
public class FranchiseHandler {

    private final FranchiseUseCase franchiseUseCase;
    private final TopStockProductUseCase topStockProductUseCase;
    private final RequestValidator validator;

    public Mono<ServerResponse> createFranchise(ServerRequest request) {
        return validator.body(request, NameRequest.class)
                .doOnNext(body -> log.info(CreateFranchise.REQUEST, kv(LogKeys.REQUEST, body)))
                .flatMap(body -> franchiseUseCase.create(body.name()))
                .map(FranchiseResponse::fromDomain)
                .doOnNext(response -> log.info(CreateFranchise.RESPONSE, kv(LogKeys.RESPONSE, response)))
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> renameFranchise(ServerRequest request) {
        return Mono.zip(validator.uuid(request, FRANCHISE_ID), validator.body(request, NameRequest.class))
                .doOnNext(consumer((franchiseId, body) -> log.info(RenameFranchise.REQUEST,
                        kv(FRANCHISE_ID, franchiseId), kv(LogKeys.REQUEST, body))))
                .flatMap(function((franchiseId, body) -> franchiseUseCase.rename(franchiseId, body.name())))
                .map(FranchiseResponse::fromDomain)
                .doOnNext(response -> log.info(RenameFranchise.RESPONSE, kv(LogKeys.RESPONSE, response)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> getTopStockProducts(ServerRequest request) {
        return validator.uuid(request, FRANCHISE_ID)
                .doOnNext(franchiseId -> log.info(TopStockProducts.REQUEST, kv(FRANCHISE_ID, franchiseId)))
                .flatMapMany(topStockProductUseCase::findTopStockProductPerBranch)
                .map(TopStockProductResponse::fromDomain)
                .collectList()
                .doOnNext(products -> log.info(TopStockProducts.RESPONSE, kv(LogKeys.RESPONSE, products)))
                .flatMap(products -> ServerResponse.ok().bodyValue(products));
    }
}
