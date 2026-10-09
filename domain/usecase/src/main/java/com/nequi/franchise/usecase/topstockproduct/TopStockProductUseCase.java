package com.nequi.franchise.usecase.topstockproduct;

import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.franchise.gateways.FranchiseRepository;
import com.nequi.franchise.model.product.BranchProductStock;
import com.nequi.franchise.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RequiredArgsConstructor
public class TopStockProductUseCase {

    private final FranchiseRepository franchiseRepository;
    private final ProductRepository productRepository;

    public Flux<BranchProductStock> findTopStockProductPerBranch(UUID franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)))
                .flatMapMany(franchise -> productRepository.findStocksGroupedByBranch(franchise.id()))
                .windowUntilChanged(BranchProductStock::branchId)
                .concatMap(branchStocks -> branchStocks.reduce(BranchProductStock::higherRanked));
    }
}
