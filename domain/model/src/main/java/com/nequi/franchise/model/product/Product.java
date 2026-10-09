package com.nequi.franchise.model.product;

import lombok.Builder;

import java.util.UUID;

@Builder(toBuilder = true)
public record Product(UUID id, UUID branchId, String name, int stock) {

    public static final int MIN_STOCK = 0;

    public static boolean isValidStock(int stock) {
        return stock >= MIN_STOCK;
    }
}
