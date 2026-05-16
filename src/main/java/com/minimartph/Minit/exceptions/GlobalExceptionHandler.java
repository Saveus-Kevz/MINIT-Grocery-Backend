// File: com/minimartph/Minit/exceptions/GlobalExceptionHandler.java
package com.minimartph.Minit.exceptions;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

  // Handle MaxUploadSizeExceededException - DON'T use @ExceptionHandler here since the parent class
  // already handles it
  // Override the parent method instead
  @Override
  protected ResponseEntity<Object> handleExceptionInternal(
      Exception ex,
      Object body,
      HttpHeaders headers,
      HttpStatusCode statusCode,
      WebRequest request) {

    if (ex instanceof MaxUploadSizeExceededException) {
      String path = ((ServletWebRequest) request).getRequest().getRequestURI();
      ErrorResponse response =
          ErrorResponse.builder()
              .code(ErrorCode.FILE_TOO_LARGE.getCode())
              .message("File size exceeds maximum allowed (10MB)")
              .status(HttpStatus.BAD_REQUEST.value())
              .error("File Too Large")
              .path(path)
              .timestamp(LocalDateTime.now())
              .build();
      return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    return super.handleExceptionInternal(ex, body, headers, statusCode, request);
  }

  // Handle custom AppException
  @ExceptionHandler(AppException.class)
  public ResponseEntity<ErrorResponse> handleAppException(
      AppException ex, HttpServletRequest request) {
    log.error("AppException occurred: {} - {}", ex.getErrorCode().getCode(), ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ex.getErrorCode().getCode())
            .message(ex.getMessage())
            .status(ex.getErrorCode().getHttpStatus().value())
            .error(ex.getErrorCode().getHttpStatus().getReasonPhrase())
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(response, ex.getErrorCode().getHttpStatus());
  }

  // Handle validation errors from @Valid
  @Override
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex,
      HttpHeaders headers,
      HttpStatusCode status,
      WebRequest request) {

    List<ErrorResponse.ValidationError> validationErrors =
        ex.getBindingResult().getFieldErrors().stream()
            .map(this::toValidationError)
            .collect(Collectors.toList());

    String path = ((ServletWebRequest) request).getRequest().getRequestURI();

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.VALIDATION_ERROR.getCode())
            .message("Validation failed: " + validationErrors.size() + " error(s)")
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .path(path)
            .timestamp(LocalDateTime.now())
            .validationErrors(validationErrors)
            .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // Handle constraint violation from @Validated
  @ExceptionHandler(ConstraintViolationException.class)
  public ResponseEntity<ErrorResponse> handleConstraintViolationException(
      ConstraintViolationException ex, HttpServletRequest request) {

    List<ErrorResponse.ValidationError> validationErrors =
        ex.getConstraintViolations().stream()
            .map(this::toValidationError)
            .collect(Collectors.toList());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.VALIDATION_ERROR.getCode())
            .message("Constraint violation: " + validationErrors.size() + " error(s)")
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Error")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .validationErrors(validationErrors)
            .build();

    return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
  }

  // Handle data integrity violations (duplicate keys, etc.)
  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
      DataIntegrityViolationException ex, HttpServletRequest request) {

    log.error("Data integrity violation: {}", ex.getMessage());

    String message = "Database constraint violation";
    if (ex.getMessage() != null && ex.getMessage().contains("Duplicate entry")) {
      message = "Duplicate entry - resource already exists";
    } else if (ex.getMessage() != null && ex.getMessage().contains("Data truncated")) {
      message = "Data truncated - value too long for column";
    }

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.DATA_INTEGRITY_VIOLATION.getCode())
            .message(message)
            .detail(ex.getMostSpecificCause().getMessage())
            .status(HttpStatus.CONFLICT.value())
            .error("Data Integrity Violation")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(response, HttpStatus.CONFLICT);
  }

  // Handle authentication errors
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ErrorResponse> handleAuthenticationException(
      AuthenticationException ex, HttpServletRequest request) {

    log.warn("Authentication failed: {}", ex.getMessage());

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.UNAUTHORIZED.getCode())
            .message(ex.getMessage() != null ? ex.getMessage() : "Authentication failed")
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Unauthorized")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  // Handle bad credentials specifically
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ErrorResponse> handleBadCredentialsException(
      BadCredentialsException ex, HttpServletRequest request) {

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.INVALID_CREDENTIALS.getCode())
            .message("Invalid username or password")
            .status(HttpStatus.UNAUTHORIZED.value())
            .error("Invalid Credentials")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
  }

  // Handle access denied
  @ExceptionHandler(AccessDeniedException.class)
  public ResponseEntity<ErrorResponse> handleAccessDeniedException(
      AccessDeniedException ex, HttpServletRequest request) {

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.FORBIDDEN.getCode())
            .message("You don't have permission to access this resource")
            .status(HttpStatus.FORBIDDEN.value())
            .error("Access Denied")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
  }

  // Handle generic runtime exceptions
  @ExceptionHandler(RuntimeException.class)
  public ResponseEntity<ErrorResponse> handleRuntimeException(
      RuntimeException ex, HttpServletRequest request) {

    log.error("RuntimeException occurred: ", ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
            .message(ex.getMessage() != null ? ex.getMessage() : "An unexpected error occurred")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // Handle generic exceptions
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleGenericException(
      Exception ex, HttpServletRequest request) {

    log.error("Unexpected error occurred: ", ex);

    ErrorResponse response =
        ErrorResponse.builder()
            .code(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
            .message("An unexpected error occurred. Please try again later.")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .path(request.getRequestURI())
            .timestamp(LocalDateTime.now())
            .build();

    return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // Helper methods
  private ErrorResponse.ValidationError toValidationError(FieldError fieldError) {
    return ErrorResponse.ValidationError.builder()
        .field(fieldError.getField())
        .message(fieldError.getDefaultMessage())
        .rejectedValue(fieldError.getRejectedValue())
        .build();
  }

  private ErrorResponse.ValidationError toValidationError(ConstraintViolation<?> violation) {
    String fieldName = violation.getPropertyPath().toString();
    if (fieldName.contains(".")) {
      fieldName = fieldName.substring(fieldName.lastIndexOf('.') + 1);
    }

    return ErrorResponse.ValidationError.builder()
        .field(fieldName)
        .message(violation.getMessage())
        .rejectedValue(violation.getInvalidValue())
        .build();
  }
}
