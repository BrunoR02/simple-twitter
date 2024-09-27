package com.project.simple.twitter.exception;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExceptionDetails {

  private LocalDateTime timestamp;

  private int status;

  private String title;

  private String details;

  @JsonProperty(value = "developer_message")
  private String developerMessage;
  
}
