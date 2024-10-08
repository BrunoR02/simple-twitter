package com.project.simple.twitter.dto.user;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccessUserDto {
  
  private String accessToken;

  private long expiresIn;
}
