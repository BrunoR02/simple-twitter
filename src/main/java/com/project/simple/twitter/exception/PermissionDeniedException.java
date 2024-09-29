package com.project.simple.twitter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@Getter
@ResponseStatus(HttpStatus.FORBIDDEN)
public class PermissionDeniedException extends RuntimeException implements GenericRequestException {

  private final String title = "Permission Denied Exception. Check documentation";

  public PermissionDeniedException(String message){
    super(message);
  }

  public HttpStatus getStatusCode() {
    return HttpStatus.FORBIDDEN;
  }
}
