package com.project.simple.twitter.dto.user;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginUserDto {

  @NotEmpty(message = "email is required")
  private String email;

  @NotEmpty(message = "password is required")
  private String password;
  
}
