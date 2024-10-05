package com.project.simple.twitter.dto.twitter;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateTwitterDto {

  private String content;

  @JsonProperty(value = "visibility")
  private String visibilityValue;

  private long likes;

}
