package com.jocotech.vendingmachine.machine;

import com.jocotech.vendingmachine.common.security.login.LoginService;
import com.jocotech.vendingmachine.product.Product;
import com.jocotech.vendingmachine.product.ProductService;
import com.jocotech.vendingmachine.user.User;
import com.jocotech.vendingmachine.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class VendingMachineService {
  private static final List<Integer> COINS = List.of(5, 10, 20, 50, 100);

  private final LoginService loginService;
  private final UserService userService;
  private final ProductService productService;

  /**
   * @should return an error if the coin is not valid
   * @should find the current user
   * @should update the deposit amount of the current user with the coin amount
   * @should return the current users total deposit amount after adding the coin
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public Mono<Integer> deposit(int coin) {
    log.debug("Attempting to deposit coin of value {}", coin);
    if (!isValid(coin)) {
      return Mono.error(InvalidCoinException::new);
    }
    return getCurrentUser()
        .flatMap(user -> {
          user.setDeposit(user.getDeposit() + coin);
          return userService.update(user);
        })
        .doOnSuccess(savedUser -> log.info("{} cents deposited to user {}", coin, savedUser.getUsername()))
        .map(User::getDeposit);
  }

  private static boolean isValid(int target) {
    return COINS.contains(target);
  }

  private Mono<User> getCurrentUser() {
    return loginService.getCurrentUserId()
        .map(UUID::toString)
        .flatMap(userService::findById);
  }

  /**
   * @should get the current user
   * @should return an error if the product is not found
   * @should return an error if there are not enough available items of the product to buy
   * @should return an error if the user doesn't have enough deposited amount to buy the product
   * @should remove the purchased amount from the product if buying is possible
   * @should remove the cost of the products from the user deposit if buying is possible
   * @should return the remaining deposit of a user with the products bought
   */
  @Transactional(propagation = Propagation.REQUIRED)
  public Mono<VendingMachineController.PurchaseResponse> buy(String productId, int amount) {
    return Mono.zip(getCurrentUser(), productService.getProductById(productId))
        .switchIfEmpty(Mono.error(InvalidProductException::new))
        .flatMap(tuple -> buy(tuple.getT1(), tuple.getT2(), amount))
        .map(tuple3 -> new VendingMachineController.PurchaseResponse(tuple3.getT1(),
            new VendingMachineController.ProductDetails(tuple3.getT3().getId().toString(),
                tuple3.getT3().getProductName(), tuple3.getT3().getCost(), amount),
            tuple3.getT2().getDeposit()));
  }

  private Mono<Tuple3<Integer, User, Product>> buy(User user, Product product, int amount) {
    if (product.getAmountAvailable() < amount) {
      return Mono.error(InsufficientProductException::new);
    }
    var price = product.getCost() * amount;
    if (price > user.getDeposit()) {
      return Mono.error(InsufficientFundsException::new);
    }
    product.setAmountAvailable(product.getAmountAvailable() - amount);
    user.setDeposit(user.getDeposit() - price);
    var userUpdateMono = userService.update(user)
        .doOnSuccess(savedUser -> log.info("{} cents deducted from user {}", price, savedUser.getUsername()));
    var productUpdateMono = productService.update(product)
        .doOnSuccess(savedProduct -> log.info("{} items removed for product {}", amount, savedProduct.getProductName()));
    return Mono.zip(Mono.just(price), userUpdateMono, productUpdateMono);
  }

  @Transactional(propagation = Propagation.REQUIRED)
  public Mono<Integer> resetDeposit() {
    log.trace("Resetting deposit.");
    return getCurrentUser()
        .flatMap(user -> {
          log.trace("Resetting deposit for user: {}", user.getUsername());
          user.setDeposit(0);
          return userService.update(user);
        })
        .doOnSuccess(savedUser -> log.info("User deposit set to 0 for user: {}", savedUser.getUsername()))
        .map(User::getDeposit);
  }
}
