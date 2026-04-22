// File: com/minimartph/Minit/exceptions/AppException.java
package com.minimartph.Minit.exceptions;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
  private final ErrorCode errorCode;
  private final String message;

  public AppException(ErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
    this.message = message;
  }

  public AppException(ErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
    this.message = message;
  }
}
