package com.project.simple.twitter.dto.user;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class UpdateUserDto {

  @JsonProperty(value = "display_name")
  private String displayName;

  @JsonProperty(value = "birth_date")
  private LocalDate birthDate;

}
