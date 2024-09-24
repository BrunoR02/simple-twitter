package com.project.simple.twitter.security;

import java.io.IOException;

import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.project.simple.twitter.service.JwtTokenService;
import com.project.simple.twitter.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class UserAuthenticationFilter extends OncePerRequestFilter {

  private final UserService userService;
  private final JwtTokenService jwtTokenService;

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
      throws ServletException, IOException {

    if (isAuthenticationRequiredOnEndpoint(request)) {
      String token = getHeaderToken(request);
      if(token == null)
        throw new AuthenticationCredentialsNotFoundException("Token is invalid");

      String tokenSubject = jwtTokenService.getTokenSubject(token);
      UserDetails userDetails = userService.loadUserByUsername(tokenSubject);

      Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails.getUsername(),null,userDetails.getAuthorities());

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(request, response);
  }

  public boolean isAuthenticationRequiredOnEndpoint(HttpServletRequest request) {
    return !SecurityConstants.NO_AUTHENTICATION_ENDPOINTS.contains(request.getRequestURI());
  }

  public String getHeaderToken(HttpServletRequest request){
    String tokenHeader = request.getHeader("Authorization");

    if(tokenHeader == null)
      return null;
    
    return tokenHeader.replace("Bearer ","");
  }

}
