package com.project.simple.twitter.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class CreateUserDto {

  @NotEmpty(message = "username is required")
  private String username;

  @NotEmpty(message = "email is required")
  @Email(message = "email is invalid")
  private String email;

  @NotEmpty(message = "password is required")
  private String password;

}
