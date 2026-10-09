package com.nequi.franchise.api.dto.response;

import com.nequi.franchise.model.product.Product;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record ProductResponse(
        @Schema(example = "c3e8a1f2-7b5d-4a9e-b2c4-6d8f0a1e3b57") UUID id,
        @Schema(example = "9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23") UUID branchId,
        @Schema(example = "Cheeseburger") String name,
        @Schema(example = "25") int stock) {

    public static ProductResponse fromDomain(Product product) {
        return new ProductResponse(product.id(), product.branchId(), product.name(), product.stock());
    }
}
