package com.jocotech.vendingmachine.product;

import com.jocotech.vendingmachine.common.security.login.LoginService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;
  private final LoginService loginService;

  public Flux<Product> getAllProducts() {
    return productRepository.findAll();
  }

  public Mono<Product> getProductById(String id) {
    return productRepository.findById(UUID.fromString(id));
  }

  public Mono<Product> createProduct(ProductController.CreateProductRequest createProductRequest) {
    // Ensure cost is in multiples of 5
    validateCost(createProductRequest.cost());
    return loginService.getCurrentUserId()
        .flatMap(sellerId -> productRepository.save(Product.builder()
            .sellerId(sellerId)
            .productName(createProductRequest.productName())
            .cost(createProductRequest.cost())
            .amountAvailable(createProductRequest.amountAvailable())
            .build()));
  }

  // Ensure cost is in multiples of 5
  private void validateCost(int cost) {
    if (cost % 5 != 0) {
      throw new IllegalArgumentException("Product cost must be in multiples of 5");
    }
  }

  public Mono<Product> update(String id, ProductController.UpdateProductRequest updateProductRequest) {
    return findProductAndValidateUserIsSeller(id)
        .flatMap(existingProduct -> update(existingProduct, updateProductRequest));
  }

  public Mono<Product> update(Product product) {
    return productRepository.save(product);
  }

  private Mono<Product> findProductAndValidateUserIsSeller(String id) {
    var productMono = productRepository.findById(UUID.fromString(id));
    var userIdMono = loginService.getCurrentUserId();
    return Mono.zip(productMono, userIdMono)
        .flatMap(tuple -> {
          var existingProduct = tuple.getT1();
          // Only allow seller to update their own products
          if (!existingProduct.getSellerId().equals(tuple.getT2())) {
            log.error("User {} not authorized to update this product {}", tuple.getT2(), id);
            return Mono.error(new AccessDeniedException("You are not authorized to update this product"));
          }
          return Mono.just(existingProduct);
        });
  }

  private Mono<Product> update(Product existingProduct,
                               ProductController.UpdateProductRequest updateProductRequest) {
    if (updateProductRequest.cost() != null) {
      validateCost(updateProductRequest.cost());
      existingProduct.setCost(updateProductRequest.cost());
    }
    if (updateProductRequest.amountAvailable() != null) {
      existingProduct.setAmountAvailable(updateProductRequest.amountAvailable());
    }
    if (StringUtils.isNotBlank(updateProductRequest.productName())) {
      existingProduct.setProductName(updateProductRequest.productName());
    }
    return productRepository.save(existingProduct);
  }

  public Mono<Void> deleteProduct(String id) {
    return findProductAndValidateUserIsSeller(id)
        .flatMap(productRepository::delete);
  }
}
