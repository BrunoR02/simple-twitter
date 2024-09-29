package com.project.simple.twitter.handler;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.project.simple.twitter.exception.BadRequestException;
import com.project.simple.twitter.exception.GenericRequestException;
import com.project.simple.twitter.exception.InvalidArgumentException;
import com.project.simple.twitter.exception.InvalidCredentialsException;
import com.project.simple.twitter.exception.NotFoundException;
import com.project.simple.twitter.exception.PermissionDeniedException;
import com.project.simple.twitter.exception.details.ExceptionDetails;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler({
    InvalidArgumentException.class,
    PermissionDeniedException.class,
    InvalidCredentialsException.class,
    BadRequestException.class,
    NotFoundException.class
  })
  public ResponseEntity<ExceptionDetails> handleInvalidArgumentException(GenericRequestException ex) {

    return new ResponseEntity<>(ExceptionDetails.parse((ex)), ex.getStatusCode());
  }

  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {

    String details = ex.getFieldErrors()
        .stream()
        .map(FieldError::getDefaultMessage)
        .collect(Collectors.joining(", "));

    ExceptionDetails exceptionDetails = ExceptionDetails.builder()
        .status(status.value())
        .title("Invalid Fields Exception. Check details")
        .timestamp(LocalDateTime.now())
        .details(details)
        .developerMessage(ex.getClass().getName())
        .build();

    return new ResponseEntity<>(exceptionDetails, status);
  }

  @Override
  protected ResponseEntity<Object> handleExceptionInternal(Exception exception, Object arg1, HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {

    ExceptionDetails exceptionDetails = ExceptionDetails.builder()
        .status(status.value())
        .title("Bad Request Exception")
        .timestamp(LocalDateTime.now())
        .details(exception.getMessage())
        .developerMessage(exception.getClass().getName())
        .build();

    return new ResponseEntity<>(exceptionDetails, status);
  }

}
