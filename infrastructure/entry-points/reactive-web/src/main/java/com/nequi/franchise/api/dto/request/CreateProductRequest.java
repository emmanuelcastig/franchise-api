package com.nequi.franchise.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record CreateProductRequest(
        @Schema(example = "Cheeseburger", maxLength = 100)
        @NotBlank
        @Size(max = 100)
        String name,

        @Schema(example = "25", minimum = "0")
        @NotNull
        @PositiveOrZero
        Integer stock) {
}
