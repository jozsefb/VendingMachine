package com.jocotech.vendingmachine.common.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jocotech.vendingmachine.common.security.InvalidUserException;
import com.jocotech.vendingmachine.machine.InvalidCoinException;
import io.jsonwebtoken.JwtException;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Configuration
@Order(-2)
public class WebfluxGlobalErrorHandler implements ErrorWebExceptionHandler {

  record HttpError(String message) {
  }

  private final ObjectMapper objectMapper;

  @NonNull
  @Override
  public Mono<Void> handle(ServerWebExchange serverWebExchange, @NonNull Throwable t) {
    ServerHttpResponse response = serverWebExchange.getResponse();
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    DataBufferFactory bufferFactory = response.bufferFactory();
    DataBuffer dataBuffer = getExceptionMessage(bufferFactory, t.getMessage());

    // handled exceptions
    if (t instanceof JwtException || t instanceof InvalidUserException) {
      // error decoding the JWT Token of the user
      response.setStatusCode(HttpStatus.UNAUTHORIZED);
    } else if (t instanceof ResponseStatusException r) {
      response.setStatusCode(r.getStatusCode());
    } else if (t instanceof InvalidCoinException || t instanceof IllegalArgumentException) {
      response.setStatusCode(HttpStatus.BAD_REQUEST);
    } else {
      // unknown exceptions
      log.error("Caught an unknown exception:", t);
      response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
    }

    return response.writeWith(Mono.just(dataBuffer));
  }

  private DataBuffer getExceptionMessage(DataBufferFactory bufferFactory, String message) {
    DataBuffer dataBuffer;
    try {
      dataBuffer = bufferFactory
          .wrap(objectMapper.writeValueAsBytes(new HttpError(message)));
    } catch (JsonProcessingException e) {
      dataBuffer = bufferFactory.wrap("".getBytes());
    }
    return dataBuffer;
  }
}
