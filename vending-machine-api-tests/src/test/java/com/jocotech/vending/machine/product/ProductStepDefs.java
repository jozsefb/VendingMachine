package com.jocotech.vending.machine.product;

import com.jocotech.vending.machine.common.CommonStepDefinitions;
import com.jocotech.vending.machine.common.RestAPIContext;
import com.jocotech.vending.machine.user.UserStepDefs;
import io.cucumber.java.en.And;
import io.cucumber.java.en.When;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@RequiredArgsConstructor
public class ProductStepDefs {
  private final RestAPIContext restAPIContext;
  private final ProductContext productContext;
  private final CommonStepDefinitions commonStepDefinitions;
  private final UserStepDefs userStepDefinitions;

  record CreateProductRequest(String productName, int cost, int amountAvailable) {
  }

  record ProductResponse(String id, String sellerId, String productName, int cost, int amountAvailable,
                         LocalDateTime createdDate, LocalDateTime lastModifiedDate) {
  }

  record UpdateProductRequest(String productName, Integer cost, Integer amountAvailable) {
  }

  @When("I set the product details {string}")
  public void iSetTheProductDetails(String name) {
    var request = new CreateProductRequest(name, 10, 100);
    restAPIContext.setRequest(request);
  }

  @And("an existing product")
  public void anExistingProduct() {
    iSetTheProductDetails("Product " + UUID.randomUUID());
    commonStepDefinitions.iPOSTTo("/products");
    var response = restAPIContext.getResponse().as(ProductResponse.class);
    productContext.setProductId(response.id());
  }

  @When("I try to update the product with {string} {string}")
  public void iTryToUpdateTheProductWithUser(String userType, String user) {
    if (!"same user".equals(user)) {
      userStepDefinitions.aLoggedIn(userType);
    }
    var request = new UpdateProductRequest(null, 20, null);
    restAPIContext.setRequest(request);
    commonStepDefinitions.iPUTTo("/products", productContext.getProductId());
  }
}
