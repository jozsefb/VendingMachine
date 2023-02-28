package com.jocotech.vendingmachine.machine;

public class InsufficientFundsException extends RuntimeException {
  public InsufficientFundsException() {
    super("Insufficient funds, please deposit more coins.");
  }
}
