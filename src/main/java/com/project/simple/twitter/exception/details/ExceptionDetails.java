package com.project.simple.twitter.exception.details;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.project.simple.twitter.exception.GenericRequestException;

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

  public static <T extends GenericRequestException> ExceptionDetails parse(T exception) {
    return ExceptionDetails.builder()
        .title(exception.getTitle())
        .status(exception.getStatusCode().value())
        .timestamp(LocalDateTime.now())
        .details(exception.getMessage())
        .developerMessage(exception.getClass().getName())
        .build();
  }
  
}
