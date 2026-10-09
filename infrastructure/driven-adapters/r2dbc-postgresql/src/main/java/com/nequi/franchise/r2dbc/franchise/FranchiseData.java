package com.nequi.franchise.r2dbc.franchise;

import com.nequi.franchise.model.franchise.Franchise;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("franchise")
public record FranchiseData(@Id UUID id, String name) {

    static FranchiseData fromDomain(Franchise franchise) {
        return new FranchiseData(franchise.id(), franchise.name());
    }

    Franchise toDomain() {
        return Franchise.builder().id(id).name(name).build();
    }
}
