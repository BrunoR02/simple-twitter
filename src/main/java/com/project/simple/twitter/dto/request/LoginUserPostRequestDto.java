package com.project.simple.twitter.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class LoginUserPostRequestDto {

  @NotEmpty(message = "email is required")
  private String email;

  @NotEmpty(message = "password is required")
  private String password;
  
}
