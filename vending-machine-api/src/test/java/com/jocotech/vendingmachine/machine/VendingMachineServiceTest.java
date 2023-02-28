package com.jocotech.vendingmachine.machine;

import com.jocotech.vendingmachine.common.security.login.LoginService;
import com.jocotech.vendingmachine.product.Product;
import com.jocotech.vendingmachine.product.ProductService;
import com.jocotech.vendingmachine.user.Role;
import com.jocotech.vendingmachine.user.User;
import com.jocotech.vendingmachine.user.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class VendingMachineServiceTest {
  private UserService userService;
  private ProductService productService;
  private VendingMachineService vendingMachineService;

  private User user;
  private String productId;
  private Product product;

  @BeforeEach
  public void setup() {
    var loginService = mock(LoginService.class);
    userService = mock(UserService.class);
    productService = mock(ProductService.class);
    vendingMachineService = new VendingMachineService(loginService, userService, productService);
    var id = UUID.randomUUID();
    doReturn(Mono.just(id)).when(loginService).getCurrentUserId();
    user = User.builder()
        .id(id)
        .username(id.toString())
        .role(Role.ROLE_BUYER)
        .deposit(30)
        .build();
    productId = UUID.randomUUID().toString();
    product = Product.builder()
        .id(UUID.fromString(productId))
        .sellerId(user.getId())
        .amountAvailable(100)
        .cost(10)
        .productName(UUID.randomUUID().toString())
        .build();
    doReturn(Mono.just(user)).when(userService).findById(id.toString());
    lenient()
        .doAnswer(invocationOnMock -> Mono.just(invocationOnMock.getArgument(0)))
        .when(userService).update(any(User.class));
    lenient()
        .doAnswer(invocationOnMock -> Mono.just(invocationOnMock.getArgument(0)))
        .when(productService).update(any(Product.class));
  }

  /**
   * @verifies return an error if the coin is not valid
   * @see VendingMachineService#deposit(int)
   */
  @Test
  void deposit_shouldReturnAnErrorIfTheCoinIsNotValid() {
    // Arrange

    // Act & Assert
    StepVerifier.create(vendingMachineService.deposit(25))
        .expectError(InvalidCoinException.class)
        .verify();

    verify(userService, times(0)).update(any(User.class));
  }

  /**
   * @verifies find the current user
   * @see VendingMachineService#deposit(int)
   */
  @Test
  void deposit_shouldFindTheCurrentUser() {
    // Arrange

    // Act & Assert
    StepVerifier.create(vendingMachineService.deposit(20))
        .expectNextCount(1)
        .verifyComplete();

    verify(userService, times(1)).findById(user.getId().toString());
  }

  /**
   * @verifies update the deposit amount of the current user with the coin amount
   * @see VendingMachineService#deposit(int)
   */
  @Test
  void deposit_shouldUpdateTheDepositAmountOfTheCurrentUserWithTheCoinAmount() {
    // Arrange

    // Act & Assert
    StepVerifier.create(vendingMachineService.deposit(20))
        .expectNextCount(1)
        .verifyComplete();

    verify(userService, times(1)).update(any(User.class));
  }

  /**
   * @verifies return the current users total deposit amount after adding the coin
   * @see VendingMachineService#deposit(int)
   */
  @Test
  void deposit_shouldReturnTheCurrentUsersTotalDepositAmountAfterAddingTheCoin() {
    // Arrange

    // Act & Assert
    StepVerifier.create(vendingMachineService.deposit(20))
        .expectNext(50)
        .verifyComplete();
  }

  /**
   * @verifies get the current user
   * @see VendingMachineService#buy(String, int)
   */
  @Test
  void buy_shouldGetTheCurrentUser() {
    // Arrange
    doReturn(Mono.empty()).when(productService).getProductById(anyString());

    // Act & Assert
    StepVerifier.create(vendingMachineService.buy(productId, 7))
        .verifyError();

    verify(userService, times(1)).findById(user.getId().toString());
  }

  /**
   * @verifies return an error if the product is not found
   * @see VendingMachineService#buy(String, int)
   */
  @Test
  void buy_shouldReturnAnErrorIfTheProductIsNotFound() {
    // Arrange
    doReturn(Mono.empty()).when(productService).getProductById(anyString());

    // Act & Assert
    StepVerifier.create(vendingMachineService.buy(productId, 7))
        .expectError(InvalidProductException.class)
        .verify();

    verify(productService, times(1)).getProductById(productId);
  }

  /**
   * @verifies return an error if there are not enough available items of the product to buy
   * @see VendingMachineService#buy(String, int)
   */
  @Test
  void buy_shouldReturnAnErrorIfThereAreNotEnoughAvailableItemsOfTheProductToBuy() {
    // Arrange
    doReturn(Mono.just(product)).when(productService).getProductById(anyString());

    // Act & Assert
    StepVerifier.create(vendingMachineService.buy(productId, 117))
        .expectError(InsufficientProductException.class)
        .verify();
  }

  /**
   * @verifies return an error if the user doesn't have enough deposited amount to buy the product
   * @see VendingMachineService#buy(String, int)
   */
  @Test
  void buy_shouldReturnAnErrorIfTheUserDoesntHaveEnoughDepositedAmountToBuyTheProduct() {
    // Arrange
    doReturn(Mono.just(product)).when(productService).getProductById(anyString());

    // Act & Assert
    StepVerifier.create(vendingMachineService.buy(productId, 17))
        .expectError(InsufficientFundsException.class)
        .verify();
  }

  /**
   * @verifies remove the purchased amount from the product if buying is possible
   * @see VendingMachineService#buy(String, int)
   */
  @Test
  void buy_shouldRemoveThePurchasedAmountFromTheProductIfBuyingIsPossible() {
    // Arrange
    doReturn(Mono.just(product)).when(productService).getProductById(anyString());

    // Act & Assert
    StepVerifier.create(vendingMachineService.buy(productId, 3))
        .expectNextCount(1)
        .verifyComplete();
    verify(productService, times(1)).update(any(Product.class));
  }

  /**
   * @verifies remove the cost of the products from the user deposit if buying is possible
   * @see VendingMachineService#buy(String, int)
   */
  @Test
  void buy_shouldRemoveTheCostOfTheProductsFromTheUserDepositIfBuyingIsPossible() {
    // Arrange
    doReturn(Mono.just(product)).when(productService).getProductById(anyString());

    // Act & Assert
    StepVerifier.create(vendingMachineService.buy(productId, 3))
        .expectNextCount(1)
        .verifyComplete();
    verify(userService, times(1)).update(any(User.class));
  }

  /**
   * @verifies return the remaining deposit of a user with the products bought
   * @see VendingMachineService#buy(String, int)
   */
  @Test
  void buy_shouldReturnTheRemainingDepositOfAUserWithTheProductsBought() {
    // Arrange
    var productName = UUID.randomUUID().toString();
    product.setProductName(productName);
    doReturn(Mono.just(product)).when(productService).getProductById(anyString());
    var expected = new VendingMachineController.PurchaseResponse(20,
        new VendingMachineController.ProductDetails(productId, productName, 10, 2),
        10);

    // Act & Assert
    StepVerifier.create(vendingMachineService.buy(productId, 2))
        .expectNext(expected)
        .verifyComplete();
  }
}
