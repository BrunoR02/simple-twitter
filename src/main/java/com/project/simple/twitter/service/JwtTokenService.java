package com.project.simple.twitter.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class JwtTokenService {

  private static final String SECRET_KEY = "/c&#Hkv7Byn)&(N";

  private static final String ISSUER = "simple_twitter";

  public static final Long EXPIRATION_TIME_IN_SECONDS = ChronoUnit.HOURS.getDuration().toSeconds() * 3;

  public String generateToken(UserDetails userDetails) {
    if (userDetails == null)
      throw new IllegalArgumentException("UserDetails cannot be null");

    return JWT.create()
        .withSubject(userDetails.getUsername())
        .withIssuer(ISSUER)
        .withIssuedAt(Instant.now())
        .withExpiresAt(getDefaultExpirationDate())
        .sign(getAlgorithm());
  }

  public String generateToken(UserDetails userDetails, Instant expiresAt) {
    if (userDetails == null)
      throw new IllegalArgumentException("UserDetails cannot be null");

    return JWT.create()
        .withSubject(userDetails.getUsername())
        .withIssuer(ISSUER)
        .withIssuedAt(Instant.now())
        .withExpiresAt(expiresAt)
        .sign(getAlgorithm());
  }

  public Algorithm getAlgorithm() {
    return Algorithm.HMAC256(SECRET_KEY);
  }

  public String getTokenSubject(String token) throws JWTVerificationException {
    if (token == null || token.isEmpty())
      throw new IllegalArgumentException("Token cannot be null or empty");

    return JWT.require(getAlgorithm())
        .withIssuer(ISSUER)
        .build()
        .verify(token)
        .getSubject();
  }

  public Instant getDefaultExpirationDate() {
    return Instant.now().plus(EXPIRATION_TIME_IN_SECONDS, ChronoUnit.SECONDS);
  }

}
