package com.jocotech.vendingmachine.common.security;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import reactor.core.publisher.Mono;

@Slf4j
@AllArgsConstructor
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
@Configuration
public class WebSecurityConfig {

  private static final String[] AUTHORISATION_ENDPOINT_WHITELIST = {
      // Healthchecks
      "/actuator/**",
      // Swagger
      "/v2/api-docs",
      "/configuration/ui,",
      "/configuration/security",
      "/swagger-resources/**",
      "/swagger-ui.html",
      "/swagger-ui/**",
      "/v3/api-docs/**",
      "/webjars/**",
  };

  private static final String[] USER_ENDPOINT_WHITELIST = {
      "/login",
      "/user"
  };

  private ReactiveAuthenticationManager authenticationManager;
  private SecurityContextRepository securityContextRepository;

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    log.info("Initialising security web filter chain.");
    return http
        .exceptionHandling()
        .authenticationEntryPoint((swe, e) ->
            Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED))
        ).accessDeniedHandler((swe, e) ->
            Mono.fromRunnable(() -> swe.getResponse().setStatusCode(HttpStatus.FORBIDDEN))
        ).and()
        .csrf().disable()
        .cors().disable()
        .formLogin().disable()
        .httpBasic().disable()
        .authenticationManager(authenticationManager)
        .securityContextRepository(securityContextRepository)
        .authorizeExchange()
        .pathMatchers(AUTHORISATION_ENDPOINT_WHITELIST).permitAll()
        .pathMatchers(HttpMethod.POST, USER_ENDPOINT_WHITELIST).permitAll()
        .anyExchange().authenticated()
        .and().build();
  }
}
