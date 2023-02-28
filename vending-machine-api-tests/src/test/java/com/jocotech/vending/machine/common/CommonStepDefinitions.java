package com.jocotech.vending.machine.common;

import com.jocotech.vending.machine.user.UserContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;

import static org.assertj.core.api.Assertions.assertThat;

@RequiredArgsConstructor
public class CommonStepDefinitions {
  private final RestAPIContext restAPIContext;
  private final UserContext userContext;
  private final RestSpecificationFactory restSpecificationFactory;

  @And("I set the authentication token for the user")
  public void iSetTheAuthenticationTokenForTheUser() {
    restSpecificationFactory.addAuthenticationToRequestSpecification(userContext.getToken());
  }

  @When("I GET {string}")
  public void iGET(String endpoint) {
    RequestSpecification request = RestAssured.given(restAPIContext.getRequestSpecification());
    Response response = request.get(endpoint);
    restAPIContext.setResponse(response);
  }

  @When("I GET {string} {string}")
  public void iGET(String endpoint, String id) {
    RequestSpecification request = RestAssured.given(restAPIContext.getRequestSpecification());
    String fullPath = endpoint + "/" + id;
    Response response = request.get(fullPath);
    restAPIContext.setResponse(response);
  }

  @When("I POST to {string}")
  public void iPOSTTo(String endpoint) {
    RequestSpecification request = RestAssured.given(restAPIContext.getRequestSpecification());
    if (restAPIContext.getRequest() != null) {
      request.body(restAPIContext.getRequest());
    }
    Response response = request.post(endpoint);
    restAPIContext.setResponse(response);
  }

  @When("I PUT to {string} {string}")
  public void iPUTTo(String endpoint, String id) {
    RequestSpecification request = RestAssured.given(restAPIContext.getRequestSpecification());
    String fullPath = endpoint + "/" + id;
    request.body(restAPIContext.getRequest());
    Response response = request.put(fullPath);
    restAPIContext.setResponse(response);
  }

  @When("I DELETE {string} {string}")
  public void iDELETE(String endpoint, String id) {
    RequestSpecification request = RestAssured.given(restAPIContext.getRequestSpecification());
    String fullPath = endpoint + "/" + id;
    Response response = request.delete(fullPath);
    restAPIContext.setResponse(response);
  }

  @Then("the response code should be {int}")
  public void theResponseCodeShouldBe(int expectedResponseCode) {
    Response response = restAPIContext.getResponse();
    assertThat(response).isNotNull();
    assertThat(response.getStatusCode()).isEqualTo(expectedResponseCode);
  }

  @Then("the response code should be {int} with {string}: {string}")
  public void theResponseCodeShouldBeWith(int expectedResponseCode, String jsonPath, String message) {
    Response response = restAPIContext.getResponse();
    assertThat(response.getStatusCode()).isEqualTo(expectedResponseCode);
    assertThat(response.getBody().jsonPath().getString(jsonPath)).hasToString(message);
  }

  @Then("the response content-type should be {string}")
  public void theResponseContentTypeShouldBe(String responseContentType) {
    Response response = restAPIContext.getResponse();
    assertThat(response.contentType()).isEqualTo(responseContentType);
  }
}
