// File: com/minimartph/Minit/exceptions/EmailSendException.java
package com.minimartph.Minit.exceptions;

public class EmailSendException extends AppException {
  public EmailSendException(String recipient, String message) {
    super(
        ErrorCode.EMAIL_SEND_FAILED,
        String.format("Failed to send email to %s: %s", recipient, message));
  }
}
