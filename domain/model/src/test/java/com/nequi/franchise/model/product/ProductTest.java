package com.nequi.franchise.model.product;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class ProductTest {

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 250, Integer.MAX_VALUE})
    @DisplayName("zero and positive stock are valid")
    void acceptsNonNegativeStock(int stock) {
        assertThat(Product.isValidStock(stock)).isTrue();
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, -100, Integer.MIN_VALUE})
    @DisplayName("negative stock is invalid")
    void rejectsNegativeStock(int stock) {
        assertThat(Product.isValidStock(stock)).isFalse();
    }
}
