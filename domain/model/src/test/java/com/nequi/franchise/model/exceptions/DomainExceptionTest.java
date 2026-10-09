package com.nequi.franchise.model.exceptions;

import com.nequi.franchise.model.enums.TechnicalMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DomainExceptionTest {

    @Test
    @DisplayName("the hierarchy is sealed to business and technical errors, which error translators rely on")
    void hierarchyIsSealed() {
        assertThat(DomainException.class.isSealed()).isTrue();
        assertThat(DomainException.class.getPermittedSubclasses())
                .containsExactlyInAnyOrder(BusinessException.class, TechnicalException.class);
    }

    @Test
    @DisplayName("a business exception exposes its catalogue entry and message, without a cause")
    void businessExceptionCarriesMessage() {
        BusinessException exception = new BusinessException(TechnicalMessage.PRODUCT_NOT_FOUND);

        assertThat(exception.getTechnicalMessage()).isEqualTo(TechnicalMessage.PRODUCT_NOT_FOUND);
        assertThat(exception).hasMessage(TechnicalMessage.PRODUCT_NOT_FOUND.getMessage()).hasNoCause();
    }

    @Test
    @DisplayName("a technical exception keeps the infrastructure cause for diagnosis")
    void technicalExceptionKeepsCause() {
        IllegalStateException cause = new IllegalStateException("connection refused");

        TechnicalException exception = new TechnicalException(TechnicalMessage.DATABASE_UNAVAILABLE, cause);

        assertThat(exception.getTechnicalMessage()).isEqualTo(TechnicalMessage.DATABASE_UNAVAILABLE);
        assertThat(exception).hasMessage(TechnicalMessage.DATABASE_UNAVAILABLE.getMessage()).hasCause(cause);
    }
}
