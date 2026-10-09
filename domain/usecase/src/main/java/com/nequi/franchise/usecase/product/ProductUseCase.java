package com.nequi.franchise.usecase.product;

import com.nequi.franchise.model.branch.Branch;
import com.nequi.franchise.model.branch.gateways.BranchRepository;
import com.nequi.franchise.model.enums.TechnicalMessage;
import com.nequi.franchise.model.exceptions.BusinessException;
import com.nequi.franchise.model.product.Product;
import com.nequi.franchise.model.product.gateways.ProductRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static com.nequi.franchise.usecase.commons.Validations.rejectIfPresent;
import static com.nequi.franchise.usecase.commons.Validations.validName;
import static com.nequi.franchise.usecase.commons.Validations.validStock;

@RequiredArgsConstructor
public class ProductUseCase {

    private final BranchRepository branchRepository;
    private final ProductRepository productRepository;

    public Mono<Product> addProduct(UUID franchiseId, UUID branchId, String name, int stock) {
        return validName(name)
                .zipWith(validStock(stock), (validName, validStock) -> Product.builder()
                        .branchId(branchId)
                        .name(validName)
                        .stock(validStock)
                        .build())
                .flatMap(product -> findBranch(franchiseId, branchId).thenReturn(product))
                .flatMap(product -> rejectIfPresent(productRepository.findByBranchIdAndName(branchId, product.name()),
                        TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS)
                        .thenReturn(product))
                .flatMap(productRepository::save);
    }

    public Mono<Void> removeProduct(UUID franchiseId, UUID branchId, UUID productId) {
        return findProduct(franchiseId, branchId, productId)
                .flatMap(product -> productRepository.deleteById(product.id()));
    }

    public Mono<Product> updateStock(UUID franchiseId, UUID branchId, UUID productId, int stock) {
        return validStock(stock)
                .flatMap(validStock -> findProduct(franchiseId, branchId, productId).thenReturn(validStock))
                .flatMap(validStock -> productRepository.updateStock(productId, validStock))
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.PRODUCT_NOT_FOUND)));
    }

    public Mono<Product> rename(UUID franchiseId, UUID branchId, UUID productId, String newName) {
        return validName(newName)
                .flatMap(validName -> findProduct(franchiseId, branchId, productId).thenReturn(validName))
                .flatMap(validName -> rejectIfPresent(productRepository.findByBranchIdAndName(branchId, validName)
                                .filter(owner -> !owner.id().equals(productId)),
                        TechnicalMessage.PRODUCT_NAME_ALREADY_EXISTS)
                        .thenReturn(validName))
                .flatMap(validName -> productRepository.updateName(productId, validName))
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.PRODUCT_NOT_FOUND)));
    }

    private Mono<Branch> findBranch(UUID franchiseId, UUID branchId) {
        return branchRepository.findByIdAndFranchiseId(branchId, franchiseId)
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.BRANCH_NOT_FOUND)));
    }

    private Mono<Product> findProduct(UUID franchiseId, UUID branchId, UUID productId) {
        return findBranch(franchiseId, branchId)
                .flatMap(branch -> productRepository.findByIdAndBranchId(productId, branch.id()))
                .switchIfEmpty(Mono.error(() -> new BusinessException(TechnicalMessage.PRODUCT_NOT_FOUND)));
    }
}
