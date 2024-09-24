package com.project.simple.twitter.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import lombok.Getter;

@Getter
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFoundException extends RuntimeException implements GenericRequestException {

  private final String title = "Not Found Exception. Check Documentation";

  public NotFoundException(String message) {
    super(message);
  }

  public HttpStatus getStatusCode() {
    return HttpStatus.NOT_FOUND;
  }
}
