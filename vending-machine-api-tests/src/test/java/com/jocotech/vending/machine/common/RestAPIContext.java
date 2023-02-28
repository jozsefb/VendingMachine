package com.jocotech.vending.machine.common;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import lombok.Data;

@Data
public class RestAPIContext {

  private RequestSpecification requestSpecification;
  private Object request;
  private Response response;
}
