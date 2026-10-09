package com.nequi.franchise.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

@Schema(description = "Absolute stock value (not an increment), which makes the update idempotent")
public record StockRequest(
        @Schema(example = "40", minimum = "0")
        @NotNull
        @PositiveOrZero
        Integer stock) {
}
