package com.project.simple.twitter.security;

import java.util.Map;

import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

public class SecurityConstants {

  public static final Map<String, String> NO_AUTHENTICATION_ENDPOINT_TO_METHOD_MAP = Map.of(
      "/users/login", "POST",
      "/users/confirm", "PATCH",
      "/users", "POST");

  public static boolean isAuthenticationEndpoint(String endpoint, String httpMethod) {
    boolean isNoAuthenticationEndpoint = NO_AUTHENTICATION_ENDPOINT_TO_METHOD_MAP.containsKey(endpoint);
    if (!isNoAuthenticationEndpoint)
      return true;

    return !NO_AUTHENTICATION_ENDPOINT_TO_METHOD_MAP.get(endpoint).equals(httpMethod);
  }

  public static AntPathRequestMatcher[] getNoAuthenticationEndpointMatchers() {
    return NO_AUTHENTICATION_ENDPOINT_TO_METHOD_MAP.entrySet().stream()
        .map((entry) -> new AntPathRequestMatcher(entry.getKey(), entry.getValue()))
        .toList().toArray(new AntPathRequestMatcher[0]);
  }

}
