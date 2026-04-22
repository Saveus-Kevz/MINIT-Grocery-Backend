// File: com/minimartph/Minit/exceptions/InvalidCredentialsException.java
package com.minimartph.Minit.exceptions;

public class InvalidCredentialsException extends AppException {
  public InvalidCredentialsException() {
    super(ErrorCode.INVALID_CREDENTIALS, "Invalid username or password");
  }

  public InvalidCredentialsException(String message) {
    super(ErrorCode.INVALID_CREDENTIALS, message);
  }
}
