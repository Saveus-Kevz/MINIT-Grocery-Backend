// File: com/minimartph/Minit/exceptions/ResourceNotFoundException.java
package com.minimartph.Minit.exceptions;

public class ResourceNotFoundException extends AppException {
  public ResourceNotFoundException(String resourceName, Object resourceId) {
    super(
        ErrorCode.RESOURCE_NOT_FOUND,
        String.format("%s not found with id: %s", resourceName, resourceId));
  }

  public ResourceNotFoundException(String message) {
    super(ErrorCode.RESOURCE_NOT_FOUND, message);
  }
}
