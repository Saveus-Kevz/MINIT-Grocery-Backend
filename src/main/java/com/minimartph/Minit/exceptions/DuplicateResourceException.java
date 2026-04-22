// File: com/minimartph/Minit/exceptions/DuplicateResourceException.java
package com.minimartph.Minit.exceptions;

public class DuplicateResourceException extends AppException {
  public DuplicateResourceException(String resourceName, String fieldName, Object fieldValue) {
    super(
        ErrorCode.DUPLICATE_RESOURCE,
        String.format("%s with %s '%s' already exists", resourceName, fieldName, fieldValue));
  }

  public DuplicateResourceException(String message) {
    super(ErrorCode.DUPLICATE_RESOURCE, message);
  }
}
