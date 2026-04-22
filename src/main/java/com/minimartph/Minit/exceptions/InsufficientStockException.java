// File: com/minimartph/Minit/exceptions/InsufficientStockException.java
package com.minimartph.Minit.exceptions;

public class InsufficientStockException extends AppException {
  public InsufficientStockException(String productName, int requested, int available) {
    super(
        ErrorCode.INSUFFICIENT_STOCK,
        String.format(
            "Insufficient stock for product '%s'. Requested: %d, Available: %d",
            productName, requested, available));
  }
}
