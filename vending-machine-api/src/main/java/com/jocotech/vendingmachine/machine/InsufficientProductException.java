package com.jocotech.vendingmachine.machine;

public class InsufficientProductException extends RuntimeException {
  public InsufficientProductException() {
    super("Not enough products available for purchase.");
  }
}
