package com.project.simple.twitter.security;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.simple.twitter.exception.ExceptionDetails;
import com.project.simple.twitter.service.JwtTokenService;
import com.project.simple.twitter.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;

@Component
@RequiredArgsConstructor
@Log4j2
public class UserAuthenticationFilter extends OncePerRequestFilter {

  private final UserService userService;
  private final JwtTokenService jwtTokenService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    String token = getHeaderToken(request);

    if (isAuthenticationRequiredOnEndpoint(request)) {
      
      try {
        UserDetails userDetails = getUserDetailsFromToken(token);

        setUserAuthentication(userDetails);
      } catch (JWTVerificationException | UsernameNotFoundException exception) {
        handleInvalidAuthentication(response, exception);
        return;
      }
    }

    filterChain.doFilter(request, response);
  }

  private UserDetails getUserDetailsFromToken(String token) throws JWTVerificationException, UsernameNotFoundException {
    if (token == null)
      throw new JWTVerificationException("Token is not valid");

    String tokenSubject = jwtTokenService.getTokenSubject(token);

    return userService.loadUserByUsername(tokenSubject);
  }

  private void setUserAuthentication(UserDetails userDetails) {
    Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null,
        userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void handleInvalidAuthentication(HttpServletResponse response, Exception exception) throws IOException {
    ExceptionDetails exceptionDetails = ExceptionDetails.builder()
        .title("Invalid Authentication Exception")
        .timestamp(LocalDateTime.now())
        .details(exception.getMessage())
        .status(HttpServletResponse.SC_UNAUTHORIZED)
        .developerMessage(exception.getClass().getName())
        .build();

    response.setContentType("application/json");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write(objectMapper.writeValueAsString(exceptionDetails));
  }

  public boolean isAuthenticationRequiredOnEndpoint(HttpServletRequest request) {
    return !(List.of(SecurityConstants.NO_AUTHENTICATION_POST_ENDPOINTS).contains(request.getRequestURI())
        && request.getMethod() == "POST");
  }

  public String getHeaderToken(HttpServletRequest request) {
    String tokenHeader = request.getHeader("Authorization");

    if (tokenHeader == null)
      return null;

    return tokenHeader.replace("Bearer ", "");
  }

}
