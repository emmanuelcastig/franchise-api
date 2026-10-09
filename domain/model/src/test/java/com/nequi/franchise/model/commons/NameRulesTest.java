package com.nequi.franchise.model.commons;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;

class NameRulesTest {

    @ParameterizedTest
    @ValueSource(strings = {"Burger Town", "A", " Downtown "})
    @DisplayName("accepts non blank names")
    void acceptsValidNames(String name) {
        assertThat(NameRules.isValid(name)).isTrue();
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\n"})
    @DisplayName("rejects missing or blank names")
    void rejectsBlankNames(String name) {
        assertThat(NameRules.isValid(name)).isFalse();
    }

    @Test
    @DisplayName("accepts exactly the maximum length and rejects one character more")
    void enforcesMaximumLength() {
        assertThat(NameRules.isValid("x".repeat(NameRules.MAX_LENGTH))).isTrue();
        assertThat(NameRules.isValid("x".repeat(NameRules.MAX_LENGTH + 1))).isFalse();
    }

    @Test
    @DisplayName("measures the length after trimming surrounding spaces")
    void ignoresSurroundingSpacesForLength() {
        assertThat(NameRules.isValid("  " + "x".repeat(NameRules.MAX_LENGTH) + "  ")).isTrue();
    }
}
