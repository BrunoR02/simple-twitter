package com.project.simple.twitter.domain;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CustomUserDetails implements UserDetails {

  private String username;

  private String password;

  private List<? extends GrantedAuthority> authorities;
  
}
