package com.project.simple.twitter.exception;

import org.springframework.http.HttpStatus;

public interface GenericRequestException{
  
  public HttpStatus getStatusCode();

  public String getMessage();

  public String getTitle();
}
