package com.jocotech.vending.machine.user;

import lombok.Data;

@Data
public class UserContext {
  private String userName;
  private String password;
  private String token;
}
