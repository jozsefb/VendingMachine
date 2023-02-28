package com.jocotech.vendingmachine.product;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class ProductController {
  private final ProductService productService;

  record CreateProductRequest(String productName, int cost, int amountAvailable) { }
  record UpdateProductRequest(@Nullable String productName, @Nullable Integer cost, @Nullable Integer amountAvailable) { }

  @GetMapping("/products")
  public Flux<Product> getAllProducts() {
    return productService.getAllProducts();
  }

  @GetMapping("/products/{productId}")
  public Mono<Product> getProductById(@PathVariable String productId) {
    return productService.getProductById(productId);
  }

  @PostMapping("/products")
  @PreAuthorize("hasRole('SELLER')")
  @ResponseStatus(value = HttpStatus.CREATED)
  public Mono<Product> addProduct(@RequestBody CreateProductRequest product) {
    return productService.createProduct(product);
  }

  @PutMapping("/products/{productId}")
  @PreAuthorize("hasRole('SELLER')")
  public Mono<Product> updateProduct(@PathVariable String productId, @RequestBody UpdateProductRequest product) {
    return productService.update(productId, product);
  }

  @DeleteMapping("/products/{productId}")
  @PreAuthorize("hasRole('SELLER')")
  public Mono<Void> deleteProduct(@PathVariable String productId) {
    return productService.deleteProduct(productId);
  }
}
