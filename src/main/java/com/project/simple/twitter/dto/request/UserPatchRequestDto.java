package com.project.simple.twitter.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

import java.time.LocalDate;

@Data
public class UserPatchRequestDto {

  @JsonProperty(value = "display_name")
  private String displayName;

  @JsonProperty(value = "birth_date")
  private LocalDate birthDate;

}
