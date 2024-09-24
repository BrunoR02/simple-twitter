package com.project.simple.twitter.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserGetRequestDto {
  
  @NotEmpty(message = "email is required")
  @Email(message = "email is invalid")
  private String email;

}
