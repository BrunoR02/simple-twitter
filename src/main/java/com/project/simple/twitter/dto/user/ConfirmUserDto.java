package com.project.simple.twitter.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ConfirmUserDto {

  @NotEmpty(message = "email is required")
  @Email(message = "email is invalid")
  private String email;

}
