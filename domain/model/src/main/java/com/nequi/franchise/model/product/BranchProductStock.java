package com.nequi.franchise.model.product;

import lombok.Builder;

import java.util.Comparator;
import java.util.UUID;
import java.util.function.BinaryOperator;

@Builder(toBuilder = true)
public record BranchProductStock(UUID branchId, String branchName, UUID productId, String productName, int stock) {

    private static final Comparator<BranchProductStock> RANKING = Comparator
            .comparingInt(BranchProductStock::stock).reversed()
            .thenComparing(BranchProductStock::productName);

    private static final BinaryOperator<BranchProductStock> BEST_RANKED = BinaryOperator.minBy(RANKING);

    public static BranchProductStock higherRanked(BranchProductStock first, BranchProductStock second) {
        return BEST_RANKED.apply(first, second);
    }
}
