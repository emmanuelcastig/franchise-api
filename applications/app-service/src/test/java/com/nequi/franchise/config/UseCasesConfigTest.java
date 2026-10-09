package com.nequi.franchise.config;

import com.nequi.franchise.model.branch.gateways.BranchRepository;
import com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import com.nequi.franchise.model.product.gateways.ProductRepository;
import com.nequi.franchise.usecase.branch.BranchUseCase;
import com.nequi.franchise.usecase.franchise.FranchiseUseCase;
import com.nequi.franchise.usecase.product.ProductUseCase;
import com.nequi.franchise.usecase.topstockproduct.TopStockProductUseCase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class UseCasesConfigTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(UseCasesConfig.class)
            .withBean(FranchiseRepository.class, () -> mock(FranchiseRepository.class))
            .withBean(BranchRepository.class, () -> mock(BranchRepository.class))
            .withBean(ProductRepository.class, () -> mock(ProductRepository.class));

    @Test
    @DisplayName("every use case is registered as a bean wired to the repository ports")
    void registersEveryUseCase() {
        contextRunner.run(context -> assertThat(context)
                .hasNotFailed()
                .hasSingleBean(FranchiseUseCase.class)
                .hasSingleBean(BranchUseCase.class)
                .hasSingleBean(ProductUseCase.class)
                .hasSingleBean(TopStockProductUseCase.class));
    }
}
