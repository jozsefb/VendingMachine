package com.jocotech.vending.machine.common;

import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;

public record Hooks(RestAPIContext restAPIContext,
                    RestSpecificationFactory restSpecificationFactory) {

  @BeforeAll
  public static void setup() {
    RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    // In case we need to log request/response al the time, the filter below will do it.
//    RestAssured.filters(new RequestLoggingFilter(), new ResponseLoggingFilter());
    RestAssured.defaultParser = Parser.JSON;
  }

  @Before
  public void beforeScenario() {
    restAPIContext.setRequestSpecification(restSpecificationFactory.createRequestSpecification());
  }
}
