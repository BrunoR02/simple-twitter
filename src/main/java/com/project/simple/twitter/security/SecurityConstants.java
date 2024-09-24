package com.project.simple.twitter.security;

import java.util.List;

public class SecurityConstants {

  public static final List<String> NO_AUTHENTICATION_ENDPOINTS = List.of(
    "/users/login",
    "users"
  );
  
}
