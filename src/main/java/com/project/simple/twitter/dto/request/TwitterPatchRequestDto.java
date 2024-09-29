package com.project.simple.twitter.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class TwitterPatchRequestDto {

  private String content;

  @JsonProperty(value = "visibility")
  private String visibilityValue;

  private long likes;

}
