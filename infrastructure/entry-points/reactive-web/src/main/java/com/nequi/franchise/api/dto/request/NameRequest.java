package com.nequi.franchise.api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Name of a franchise, branch or product")
public record NameRequest(
        @Schema(example = "Burger Town", maxLength = 100)
        @NotBlank
        @Size(max = 100)
        String name) {
}
