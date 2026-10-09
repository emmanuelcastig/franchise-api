package com.nequi.franchise.api.dto.response;

import com.nequi.franchise.model.branch.Branch;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public record BranchResponse(
        @Schema(example = "9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23") UUID id,
        @Schema(example = "4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10") UUID franchiseId,
        @Schema(example = "Downtown") String name) {

    public static BranchResponse fromDomain(Branch branch) {
        return new BranchResponse(branch.id(), branch.franchiseId(), branch.name());
    }
}
