package com.jocotech.vendingmachine.machine;

public class InvalidCoinException extends RuntimeException {

  public InvalidCoinException() {
    super("Please try with a different coin!");
  }
}
