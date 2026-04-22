// File: com/minimartph/Minit/exceptions/FileStorageException.java
package com.minimartph.Minit.exceptions;

public class FileStorageException extends AppException {
  public FileStorageException(String message) {
    super(ErrorCode.FILE_STORAGE_ERROR, message);
  }

  public FileStorageException(String message, Throwable cause) {
    super(ErrorCode.FILE_STORAGE_ERROR, message, cause);
  }
}
