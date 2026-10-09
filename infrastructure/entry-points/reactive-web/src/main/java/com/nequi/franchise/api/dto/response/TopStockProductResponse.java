package com.nequi.franchise.api.dto.response;

import com.nequi.franchise.model.product.BranchProductStock;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Product with the highest stock of a branch")
public record TopStockProductResponse(
        @Schema(example = "9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23") UUID branchId,
        @Schema(example = "Downtown") String branchName,
        @Schema(example = "c3e8a1f2-7b5d-4a9e-b2c4-6d8f0a1e3b57") UUID productId,
        @Schema(example = "Fries") String productName,
        @Schema(example = "120") int stock) {

    public static TopStockProductResponse fromDomain(BranchProductStock stock) {
        return new TopStockProductResponse(
                stock.branchId(), stock.branchName(), stock.productId(), stock.productName(), stock.stock());
    }
}
