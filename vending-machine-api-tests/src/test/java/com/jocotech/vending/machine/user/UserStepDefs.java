package com.jocotech.vending.machine.user;

import com.jocotech.vending.machine.common.CommonStepDefinitions;
import com.jocotech.vending.machine.common.RestAPIContext;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import lombok.RequiredArgsConstructor;

import java.util.UUID;

@RequiredArgsConstructor
public class UserStepDefs {
  private final RestAPIContext restAPIContext;
  private final UserContext userContext;
  private final CommonStepDefinitions commonStepDefinitions;

  record CreateUserRequest(String username, String password, String role) {}
  record LoginRequest(String username, String password) {}
  record LoginResponse(String accessToken, Long expiresIn, String tokenType) {}

  @Given("a logged in {string}")
  public void aLoggedIn(String userType) {
    anExisting(userType);
    iLogin();
    commonStepDefinitions.iSetTheAuthenticationTokenForTheUser();
  }

  @Given("an existing {string}")
  public void anExisting(String userType) {
    var userRequest = new CreateUserRequest("test" + UUID.randomUUID(),
        UUID.randomUUID().toString(), "ROLE_" + userType);
    userContext.setUserName(userRequest.username());
    userContext.setPassword(userRequest.password());
    restAPIContext.setRequest(userRequest);
    commonStepDefinitions.iPOSTTo("/user");
  }

  @And("I login")
  public void iLogin() {
    var loginRequest = new LoginRequest(userContext.getUserName(), userContext.getPassword());
    restAPIContext.setRequest(loginRequest);
    commonStepDefinitions.iPOSTTo("/login");
    var response = restAPIContext.getResponse().as(LoginResponse.class);
    userContext.setToken(response.accessToken());
  }
}
