package com.jocotech.vending.machine.common;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;

public record RestSpecificationFactory(RestAPIContext restAPIContext) {
  public RequestSpecification createRequestSpecification() {
    return new RequestSpecBuilder()
        .setBaseUri("http://localhost")
        .setPort(8080)
        .setContentType(ContentType.JSON)
        .setAccept(ContentType.ANY)
        .build();
  }

  public void addAuthenticationToRequestSpecification(String token) {
    var requestSpecification = restAPIContext.getRequestSpecification();
    requestSpecification.auth().oauth2(token);
    restAPIContext.setRequestSpecification(requestSpecification);
  }
}
