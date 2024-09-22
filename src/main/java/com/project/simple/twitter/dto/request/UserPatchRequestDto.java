package com.project.simple.twitter.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class UserPatchRequestDto {

  @NotEmpty(message="email is required")
  private String email;

  @JsonProperty(value = "display_name")
  private String displayName;

  @JsonProperty(value = "birth_date")
  private String birthDate;

}
