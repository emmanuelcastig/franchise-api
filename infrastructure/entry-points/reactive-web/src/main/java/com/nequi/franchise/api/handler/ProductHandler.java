package com.nequi.franchise.api.handler;

import com.nequi.franchise.api.dto.request.CreateProductRequest;
import com.nequi.franchise.api.dto.request.NameRequest;
import com.nequi.franchise.api.dto.request.StockRequest;
import com.nequi.franchise.api.dto.response.ProductResponse;
import com.nequi.franchise.api.utils.Constants.LogKeys;
import com.nequi.franchise.api.utils.Constants.LogMessages.AddProduct;
import com.nequi.franchise.api.utils.Constants.LogMessages.RemoveProduct;
import com.nequi.franchise.api.utils.Constants.LogMessages.RenameProduct;
import com.nequi.franchise.api.utils.Constants.LogMessages.UpdateStock;
import com.nequi.franchise.api.validation.RequestValidator;
import com.nequi.franchise.usecase.product.ProductUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.nequi.franchise.api.ApiPaths.BRANCH_ID;
import static com.nequi.franchise.api.ApiPaths.FRANCHISE_ID;
import static com.nequi.franchise.api.ApiPaths.PRODUCT_ID;
import static net.logstash.logback.argument.StructuredArguments.kv;
import static reactor.function.TupleUtils.consumer;
import static reactor.function.TupleUtils.function;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductHandler {

    private final ProductUseCase productUseCase;
    private final RequestValidator validator;

    public Mono<ServerResponse> addProduct(ServerRequest request) {
        return Mono.zip(
                        validator.uuid(request, FRANCHISE_ID),
                        validator.uuid(request, BRANCH_ID),
                        validator.body(request, CreateProductRequest.class))
                .doOnNext(consumer((franchiseId, branchId, body) -> log.info(AddProduct.REQUEST,
                        kv(FRANCHISE_ID, franchiseId), kv(BRANCH_ID, branchId), kv(LogKeys.REQUEST, body))))
                .flatMap(function((franchiseId, branchId, body) ->
                        productUseCase.addProduct(franchiseId, branchId, body.name(), body.stock())))
                .map(ProductResponse::fromDomain)
                .doOnNext(response -> log.info(AddProduct.RESPONSE, kv(LogKeys.RESPONSE, response)))
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        return Mono.zip(
                        validator.uuid(request, FRANCHISE_ID),
                        validator.uuid(request, BRANCH_ID),
                        validator.uuid(request, PRODUCT_ID))
                .doOnNext(consumer((franchiseId, branchId, productId) -> log.info(RemoveProduct.REQUEST,
                        kv(FRANCHISE_ID, franchiseId), kv(BRANCH_ID, branchId), kv(PRODUCT_ID, productId))))
                .flatMap(function(productUseCase::removeProduct))
                .doOnSuccess(removed -> log.info(RemoveProduct.RESPONSE))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        return Mono.zip(
                        validator.uuid(request, FRANCHISE_ID),
                        validator.uuid(request, BRANCH_ID),
                        validator.uuid(request, PRODUCT_ID),
                        validator.body(request, StockRequest.class))
                .doOnNext(consumer((franchiseId, branchId, productId, body) -> log.info(UpdateStock.REQUEST,
                        kv(FRANCHISE_ID, franchiseId), kv(BRANCH_ID, branchId), kv(PRODUCT_ID, productId),
                        kv(LogKeys.REQUEST, body))))
                .flatMap(function((franchiseId, branchId, productId, body) ->
                        productUseCase.updateStock(franchiseId, branchId, productId, body.stock())))
                .map(ProductResponse::fromDomain)
                .doOnNext(response -> log.info(UpdateStock.RESPONSE, kv(LogKeys.RESPONSE, response)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> renameProduct(ServerRequest request) {
        return Mono.zip(
                        validator.uuid(request, FRANCHISE_ID),
                        validator.uuid(request, BRANCH_ID),
                        validator.uuid(request, PRODUCT_ID),
                        validator.body(request, NameRequest.class))
                .doOnNext(consumer((franchiseId, branchId, productId, body) -> log.info(RenameProduct.REQUEST,
                        kv(FRANCHISE_ID, franchiseId), kv(BRANCH_ID, branchId), kv(PRODUCT_ID, productId),
                        kv(LogKeys.REQUEST, body))))
                .flatMap(function((franchiseId, branchId, productId, body) ->
                        productUseCase.rename(franchiseId, branchId, productId, body.name())))
                .map(ProductResponse::fromDomain)
                .doOnNext(response -> log.info(RenameProduct.RESPONSE, kv(LogKeys.RESPONSE, response)))
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}
