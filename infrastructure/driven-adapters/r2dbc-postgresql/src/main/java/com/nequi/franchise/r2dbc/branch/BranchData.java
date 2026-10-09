package com.nequi.franchise.r2dbc.branch;

import com.nequi.franchise.model.branch.Branch;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("branch")
public record BranchData(@Id UUID id, UUID franchiseId, String name) {

    static BranchData fromDomain(Branch branch) {
        return new BranchData(branch.id(), branch.franchiseId(), branch.name());
    }

    Branch toDomain() {
        return Branch.builder().id(id).franchiseId(franchiseId).name(name).build();
    }
}
