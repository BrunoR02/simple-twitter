package com.project.simple.twitter.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class UserGetResponseDto {

  private String username;

  private int age;

  @JsonProperty(value = "display_name")
  private String displayName;

  @JsonProperty(value = "create_date")
  private LocalDate createDate;

  @JsonProperty(value = "account_status")
  private String accountStatus;
  
}
