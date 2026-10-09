package com.nequi.franchise.r2dbc.product;

import com.nequi.franchise.model.product.Product;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table("product")
public record ProductData(@Id UUID id, UUID branchId, String name, int stock) {

    static ProductData fromDomain(Product product) {
        return new ProductData(product.id(), product.branchId(), product.name(), product.stock());
    }

    Product toDomain() {
        return Product.builder().id(id).branchId(branchId).name(name).stock(stock).build();
    }
}
