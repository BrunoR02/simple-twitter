package com.project.simple.twitter.exception;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ExceptionDetails {

  private LocalDateTime timestamp;

  private int status;

  private String title;

  private String details;

  private String developerMessage;
  
}
