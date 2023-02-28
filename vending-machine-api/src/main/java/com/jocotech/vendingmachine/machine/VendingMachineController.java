package com.jocotech.vendingmachine.machine;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VendingMachineController {
  private final VendingMachineService vendingMachineService;

  record DepositRequest(int coin) {}
  record DepositResponse(int deposit) {}
  record PurchaseRequest(String productId, int amount) {}
  record ProductDetails(String productId, String productName, int price, int amountBought) {}
  record PurchaseResponse(int amountSpent, ProductDetails product, int change) {}

  @PostMapping(path = "/deposit")
  @PreAuthorize("hasRole('BUYER')")
  public Mono<DepositResponse> deposit(@RequestBody DepositRequest request) {
    log.info("Received deposit request for: {}", request);
    return vendingMachineService.deposit(request.coin())
        .map(DepositResponse::new);
  }

  @PostMapping(path = "/buy")
  @PreAuthorize("hasRole('BUYER')")
  public Mono<PurchaseResponse> buy(@RequestBody PurchaseRequest request) {
    log.info("Received buy request for: {}", request);
    return vendingMachineService.buy(request.productId(), request.amount());
  }

  @PostMapping(path = "/reset")
  @PreAuthorize("hasRole('BUYER')")
  public Mono<DepositResponse> reset() {
    log.info("Received reset request for");
    return vendingMachineService.resetDeposit()
        .map(DepositResponse::new);
  }
}
