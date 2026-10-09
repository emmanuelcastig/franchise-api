package com.nequi.franchise.api.handler;

import com.nequi.franchise.api.dto.request.CreateProductRequest;
import com.nequi.franchise.api.dto.request.NameRequest;
import com.nequi.franchise.api.dto.request.StockRequest;
import com.nequi.franchise.api.dto.response.ProductResponse;
import com.nequi.franchise.api.validation.RequestValidator;
import com.nequi.franchise.usecase.product.ProductUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import static com.nequi.franchise.api.ApiPaths.BRANCH_ID;
import static com.nequi.franchise.api.ApiPaths.FRANCHISE_ID;
import static com.nequi.franchise.api.ApiPaths.PRODUCT_ID;
import static reactor.function.TupleUtils.function;

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
                .flatMap(function((franchiseId, branchId, body) ->
                        productUseCase.addProduct(franchiseId, branchId, body.name(), body.stock())))
                .map(ProductResponse::fromDomain)
                .flatMap(response -> ServerResponse.status(HttpStatus.CREATED).bodyValue(response));
    }

    public Mono<ServerResponse> removeProduct(ServerRequest request) {
        return Mono.zip(
                        validator.uuid(request, FRANCHISE_ID),
                        validator.uuid(request, BRANCH_ID),
                        validator.uuid(request, PRODUCT_ID))
                .flatMap(function(productUseCase::removeProduct))
                .then(ServerResponse.noContent().build());
    }

    public Mono<ServerResponse> updateStock(ServerRequest request) {
        return Mono.zip(
                        validator.uuid(request, FRANCHISE_ID),
                        validator.uuid(request, BRANCH_ID),
                        validator.uuid(request, PRODUCT_ID),
                        validator.body(request, StockRequest.class))
                .flatMap(function((franchiseId, branchId, productId, body) ->
                        productUseCase.updateStock(franchiseId, branchId, productId, body.stock())))
                .map(ProductResponse::fromDomain)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }

    public Mono<ServerResponse> renameProduct(ServerRequest request) {
        return Mono.zip(
                        validator.uuid(request, FRANCHISE_ID),
                        validator.uuid(request, BRANCH_ID),
                        validator.uuid(request, PRODUCT_ID),
                        validator.body(request, NameRequest.class))
                .flatMap(function((franchiseId, branchId, productId, body) ->
                        productUseCase.rename(franchiseId, branchId, productId, body.name())))
                .map(ProductResponse::fromDomain)
                .flatMap(response -> ServerResponse.ok().bodyValue(response));
    }
}
