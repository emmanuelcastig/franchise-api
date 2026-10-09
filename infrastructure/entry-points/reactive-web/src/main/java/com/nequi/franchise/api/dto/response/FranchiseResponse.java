package com.nequi.franchise.api.dto.response;

import com.nequi.franchise.model.franchise.Franchise;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record FranchiseResponse(
        @Schema(example = "4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10") UUID id,
        @Schema(example = "Burger Town") String name) {

    public static FranchiseResponse fromDomain(Franchise franchise) {
        return new FranchiseResponse(franchise.id(), franchise.name());
    }
}
