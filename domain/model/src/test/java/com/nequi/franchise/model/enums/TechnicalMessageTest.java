package com.nequi.franchise.model.enums;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class TechnicalMessageTest {

    @Test
    @DisplayName("every error code is unique, because clients branch on it")
    void codesAreUnique() {
        Set<String> codes = new HashSet<>();

        EnumSet.allOf(TechnicalMessage.class).forEach(message -> assertThat(codes.add(message.getCode()))
                .as("duplicated code %s", message.getCode())
                .isTrue());
    }

    @Test
    @DisplayName("every error has a code with the service prefix, a message and a category")
    void messagesAreComplete() {
        EnumSet.allOf(TechnicalMessage.class).forEach(message -> {
            assertThat(message.getCode()).startsWith("FRN-");
            assertThat(message.getMessage()).isNotBlank();
            assertThat(message.getType()).isNotNull();
        });
    }

    @Test
    @DisplayName("every error category is used by at least one message")
    void everyCategoryIsUsed() {
        Set<ErrorType> used = EnumSet.noneOf(ErrorType.class);

        EnumSet.allOf(TechnicalMessage.class).forEach(message -> used.add(message.getType()));

        assertThat(used).containsExactlyInAnyOrder(ErrorType.values());
    }
}
