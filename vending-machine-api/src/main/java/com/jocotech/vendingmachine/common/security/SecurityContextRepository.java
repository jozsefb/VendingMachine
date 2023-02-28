package com.jocotech.vendingmachine.common.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
@RequiredArgsConstructor
public class SecurityContextRepository implements ServerSecurityContextRepository {
  private final ReactiveAuthenticationManager authenticationManager;

  @Override
  public Mono<Void> save(ServerWebExchange serverWebExchange, SecurityContext securityContext) {
    log.trace("Save security context");
    throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public Mono<SecurityContext> load(ServerWebExchange serverWebExchange) {
    log.trace("Load security context");
    return Mono
        .justOrEmpty(serverWebExchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
        .filter(authHeader -> authHeader.startsWith("Bearer "))
        .map(authHeader -> authHeader.substring(7))
        .map(authToken -> new PreAuthenticatedAuthenticationToken(authToken, authToken))
        .flatMap(authenticationManager::authenticate)
        .map(SecurityContextImpl::new);
  }
}
