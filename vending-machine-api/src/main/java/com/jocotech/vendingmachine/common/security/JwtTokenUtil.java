package com.jocotech.vendingmachine.common.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

@Component
public class JwtTokenUtil {

  private final Key key;

  @Getter
  @Value("${jwt.token.validity}")
  private long tokenValidity;

  // get secret from secrets manager instead
  public JwtTokenUtil(@Value("${jwt.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes());
  }

  public String generateToken(UUID userId) {
    Map<String, Object> claims = new HashMap<>();
    return createToken(claims, userId.toString());
  }

  private String createToken(Map<String, Object> claims, String subject) {
    long now = System.currentTimeMillis();
    long expirationTime = now + tokenValidity * 1000;

    return Jwts.builder()
        .setClaims(claims)
        .setSubject(subject)
        .setIssuedAt(new Date(now))
        .setExpiration(new Date(expirationTime))
        .signWith(key, SignatureAlgorithm.HS256)
        .compact();
  }

  public String extractUserId(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
  }
}
