package com.jocotech.vendingmachine.machine;

public class InvalidProductException extends RuntimeException {
  public InvalidProductException() {
    super("Invalid product selected.");
  }
}
