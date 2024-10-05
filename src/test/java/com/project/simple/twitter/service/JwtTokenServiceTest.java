package com.project.simple.twitter.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.project.simple.twitter.domain.CustomUserDetails;

@ExtendWith(SpringExtension.class)
@DisplayName("JwtTokenService Test")
public class JwtTokenServiceTest {

  @InjectMocks
  private JwtTokenService jwtTokenService;

  @Test
  @DisplayName("generateToken should return valid token when userDetails is valid")
  void generateToken_ShouldReturnToken_WhenUserDetailsIsValid() {
    // Arrange
    String subject = "brunolucas";
    UserDetails userDetails = CustomUserDetails.builder()
        .username(subject)
        .build();

    // Act
    String token = jwtTokenService.generateToken(userDetails);

    // Assert
    Assertions.assertThat(token).isNotEmpty();
    Assertions.assertThatNoException().isThrownBy(() -> jwtTokenService.getTokenSubject(token));
  }

  @Test
  @DisplayName("generateToken should throw IllegalArgumentException when userDetails is null")
  void generateToken_ShouldThrowIllegalArgumentException_WhenUserDetailsIsNull() {

    // Act & Assert
    Assertions.assertThatThrownBy(() -> jwtTokenService.generateToken(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("UserDetails cannot be null");
  }

  @Test
  @DisplayName("getTokenSubject should return subject when token is valid")
  void getTokenSubject_ShouldReturnSubject_WhenTokenIsValid() {
    // Arrange
    String subject = "brunolucas";
    String token = getToken(subject);

    // Act
    String tokenSubject = jwtTokenService.getTokenSubject(token);

    // Assert
    Assertions.assertThat(tokenSubject).isNotEmpty();
    Assertions.assertThat(tokenSubject).isEqualTo(subject);
  }

  @Test
  @DisplayName("getTokenSubject should throw IllegalArgumentException when token is null or empty")
  void getTokenSubject_ShouldThrowIllegalArgumentException_WhenTokenIsNullOrEmpty() {

    // Act & Assert
    Assertions.assertThatThrownBy(() -> jwtTokenService.getTokenSubject(null))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Token cannot be null or empty");

    // Act & Assert
    Assertions.assertThatThrownBy(() -> jwtTokenService.getTokenSubject(""))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Token cannot be null or empty");
  }

  @Test
  @DisplayName("getTokenSubject should throw JWTVerificationException when token is invalid")
  void getTokenSubject_ShouldThrowJWTVerificationException_WhenTokenIsInvalid() {
    // Arrange
    String expiredToken = getExpiredToken("brunolucas");

    // Act & Assert (Expired Token)
    Assertions.assertThatThrownBy(() -> jwtTokenService.getTokenSubject(expiredToken))
        .isInstanceOf(JWTVerificationException.class)
        .hasMessageStartingWith("The Token has expired");

    // Act & Assert (Invalid Token)
    Assertions.assertThatThrownBy(() -> jwtTokenService.getTokenSubject("asdnkjalsda"))
        .isInstanceOf(JWTVerificationException.class);
  }

  private String getToken(String subject) {
    UserDetails userDetails = CustomUserDetails.builder()
        .username(subject)
        .build();

    return jwtTokenService.generateToken(userDetails);
  }

  private String getExpiredToken(String subject) {
    UserDetails userDetails = CustomUserDetails.builder()
        .username(subject)
        .build();

    Instant pastTime = Instant.now().minus(3, ChronoUnit.HOURS);

    return jwtTokenService.generateToken(userDetails, pastTime);
  }
}
