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

    /**
     * Emits, for every branch of the franchise that has products, the product with the highest stock.
     * <p>
     * Rows arrive grouped by branch (port contract), so windowUntilChanged closes each window as soon as the
     * branch changes: memory stays constant no matter how many branches exist, and concatMap requests one window
     * at a time, which propagates the subscriber demand back to the database cursor.
     */
    public Flux<BranchProductStock> findTopStockProductPerBranch(UUID franchiseId) {
        return franchiseRepository.findById(franchiseId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.FRANCHISE_NOT_FOUND)))
                .flatMapMany(franchise -> productRepository.findStocksByFranchiseId(franchise.id()))
                .windowUntilChanged(BranchProductStock::branchId)
                .concatMap(branchStocks -> branchStocks.reduce(BranchProductStock::higherRanked));
    }
}
