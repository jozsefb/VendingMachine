package com.jocotech.vendingmachine.common.security.login;

import com.jocotech.vendingmachine.common.security.InvalidUserException;
import com.jocotech.vendingmachine.common.security.JwtTokenUtil;
import com.jocotech.vendingmachine.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class LoginService {
  private final UserService userService;
  private final JwtTokenUtil jwtTokenUtil;
  private final PasswordEncoder passwordEncoder;

  public Mono<LoginController.LoginResponse> login(@RequestBody LoginController.LoginRequest request) {
    log.info("Attempting authentication for: {}", request.username());
    return userService.findByUsername(request.username())
        .filter(user -> passwordEncoder.matches(request.password(), user.getPassword()))
        .switchIfEmpty(Mono.error(InvalidUserException::new))
        .map(user -> jwtTokenUtil.generateToken(user.getId()))
        .map(token -> new LoginController.LoginResponse(token, jwtTokenUtil.getTokenValidity(), "Bearer"));
  }

  public Mono<UUID> getCurrentUserId() {
    return ReactiveSecurityContextHolder.getContext()
        .map(SecurityContext::getAuthentication)
        .map(authentication -> (UUID) authentication.getPrincipal());
  }
}
