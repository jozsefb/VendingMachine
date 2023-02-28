package com.jocotech.vendingmachine.common.security;

import com.jocotech.vendingmachine.user.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;


@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultAuthenticationManager implements ReactiveAuthenticationManager {

  private final UserService userService;
  private final JwtTokenUtil jwtTokenUtil;

  @Override
  public Mono<Authentication> authenticate(Authentication authentication) {
    String authToken = authentication.getCredentials().toString();
    log.trace("Parsing and authenticating request with auth token: [{}]", authToken);
    String userId = jwtTokenUtil.extractUserId(authToken);
    return userService.findById(userId)
        .map(user -> {
          var roles = List.of(new SimpleGrantedAuthority(user.getRole().name()));
          return new PreAuthenticatedAuthenticationToken(user.getId(), null, roles);
        });
  }
}
