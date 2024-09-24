package com.project.simple.twitter.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginUserPostResponseDto {
  
  private String accessToken;

  private long expiresIn;
}
