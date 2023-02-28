package com.jocotech.vendingmachine.common.security;

public class InvalidUserException extends RuntimeException {

  public InvalidUserException() {
    super("Bad username or password!");
  }
}
