package com.jocotech.vendingmachine.common.security.login;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@Slf4j
@RestController
@RequiredArgsConstructor
public class LoginController {
  private final LoginService loginService;

  record LoginRequest(String username, String password) {}
  record LoginResponse(String accessToken, Long expiresIn, String tokenType) {}

  @PostMapping("/login")
  public Mono<LoginResponse> login(@RequestBody LoginRequest request) {
    log.info("Attempting authentication for: {}", request.username());
    return loginService.login(request);
  }
}
