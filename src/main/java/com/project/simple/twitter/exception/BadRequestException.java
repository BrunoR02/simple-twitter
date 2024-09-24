package com.project.simple.twitter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException implements GenericRequestException{

  private final String title = "Bad Request Exception. Check Documentation";

  public BadRequestException(String message){
    super(message);
  }

  public HttpStatus getStatusCode(){
    return HttpStatus.BAD_REQUEST;
  }
  
}
