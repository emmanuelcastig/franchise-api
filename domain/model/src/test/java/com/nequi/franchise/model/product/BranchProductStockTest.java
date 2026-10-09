package com.nequi.franchise.model.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BranchProductStockTest {

    private static final UUID BRANCH_ID = UUID.randomUUID();

    private static BranchProductStock stock(String productName, int stock) {
        return new BranchProductStock(BRANCH_ID, "Downtown", UUID.randomUUID(), productName, stock);
    }

    @Test
    @DisplayName("the product with the higher stock ranks first, regardless of argument order")
    void higherStockWins() {
        BranchProductStock fries = stock("Fries", 120);
        BranchProductStock soda = stock("Soda", 80);

        assertThat(BranchProductStock.higherRanked(fries, soda)).isSameAs(fries);
        assertThat(BranchProductStock.higherRanked(soda, fries)).isSameAs(fries);
    }

    @Test
    @DisplayName("on equal stock the alphabetically first product name wins, so the result is deterministic")
    void tiesAreBrokenByProductName() {
        BranchProductStock applePie = stock("Apple Pie", 20);
        BranchProductStock brownie = stock("Brownie", 20);

        assertThat(BranchProductStock.higherRanked(applePie, brownie)).isSameAs(applePie);
        assertThat(BranchProductStock.higherRanked(brownie, applePie)).isSameAs(applePie);
    }

    @Test
    @DisplayName("zero stock still ranks below any positive stock")
    void zeroStockRanksLast() {
        BranchProductStock empty = stock("Ice Cream", 0);
        BranchProductStock shake = stock("Shake", 1);

        assertThat(BranchProductStock.higherRanked(empty, shake)).isSameAs(shake);
    }
}
