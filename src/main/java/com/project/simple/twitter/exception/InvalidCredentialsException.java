package com.project.simple.twitter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@Getter
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class InvalidCredentialsException extends RuntimeException implements GenericRequestException {

  private final String title = "Invalid Credentials Exception. Check Details";
  
  public InvalidCredentialsException(String message){
    super(message);
  }

  public HttpStatus getStatusCode(){
    return HttpStatus.UNAUTHORIZED;
  }
}
