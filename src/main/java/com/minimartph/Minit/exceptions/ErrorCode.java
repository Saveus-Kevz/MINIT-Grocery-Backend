// File: com/minimartph/Minit/exceptions/ErrorCode.java
package com.minimartph.Minit.exceptions;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
  // Generic Errors (1000-1999)
  INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 1000, "Internal server error occurred"),
  BAD_REQUEST(HttpStatus.BAD_REQUEST, 1001, "Invalid request parameters"),
  VALIDATION_ERROR(HttpStatus.BAD_REQUEST, 1002, "Validation failed"),
  RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, 1003, "Resource not found"),
  UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 1004, "Unauthorized access"),
  FORBIDDEN(HttpStatus.FORBIDDEN, 1005, "Access denied"),
  DUPLICATE_RESOURCE(HttpStatus.CONFLICT, 1006, "Resource already exists"),
  DATA_INTEGRITY_VIOLATION(HttpStatus.CONFLICT, 1007, "Data integrity violation"),

  // User Errors (2000-2099)
  USER_NOT_FOUND(HttpStatus.NOT_FOUND, 2000, "User not found"),
  USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 2001, "Username already exists"),
  EMAIL_ALREADY_EXISTS(HttpStatus.CONFLICT, 2002, "Email already exists"),
  USER_INACTIVE(HttpStatus.FORBIDDEN, 2003, "User account is inactive"),
  USER_HAS_SALES_RECORDS(HttpStatus.BAD_REQUEST, 2004, "User has existing sales records"),
  CANNOT_DELETE_OWN_ACCOUNT(HttpStatus.BAD_REQUEST, 2005, "Cannot delete your own account"),
  CANNOT_DELETE_LAST_ADMIN(HttpStatus.BAD_REQUEST, 2006, "Cannot delete the only remaining admin"),

  // Authentication Errors (2100-2199)
  INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, 2100, "Invalid username or password"),
  TOKEN_EXPIRED(HttpStatus.UNAUTHORIZED, 2101, "Token has expired"),
  TOKEN_INVALID(HttpStatus.UNAUTHORIZED, 2102, "Invalid token"),
  NOT_AUTHENTICATED(HttpStatus.UNAUTHORIZED, 2103, "Not authenticated"),
  PASSWORD_MISMATCH(HttpStatus.BAD_REQUEST, 2104, "Password confirmation does not match"),

  // Product Errors (3000-3099)
  PRODUCT_NOT_FOUND(HttpStatus.NOT_FOUND, 3000, "Product not found"),
  PRODUCT_BARCODE_ALREADY_EXISTS(HttpStatus.CONFLICT, 3001, "Product barcode already exists"),
  PRODUCT_NAME_ALREADY_EXISTS(HttpStatus.CONFLICT, 3002, "Product name already exists"),
  INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, 3003, "Insufficient stock available"),
  INVALID_PRICE(HttpStatus.BAD_REQUEST, 3004, "Invalid price value"),
  NEGATIVE_STOCK(HttpStatus.BAD_REQUEST, 3005, "Stock quantity cannot be negative"),
  PRODUCT_HAS_SALES_RECORDS(
      HttpStatus.BAD_REQUEST, 3006, "Cannot delete product with existing sales records"),

  // Sale Errors (4000-4099)
  SALE_NOT_FOUND(HttpStatus.NOT_FOUND, 4000, "Sale not found"),
  SALE_ALREADY_VOIDED(HttpStatus.BAD_REQUEST, 4001, "Sale has already been voided"),
  INVALID_DISCOUNT_TYPE(HttpStatus.BAD_REQUEST, 4002, "Invalid discount type"),

  // File Upload Errors (5000-5099)
  FILE_EMPTY(HttpStatus.BAD_REQUEST, 5000, "File is empty"),
  FILE_TOO_LARGE(HttpStatus.BAD_REQUEST, 5001, "File size exceeds maximum allowed"),
  INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, 5002, "Invalid file type"),
  FILE_STORAGE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 5003, "Failed to store file"),
  FILE_NOT_FOUND(HttpStatus.NOT_FOUND, 5004, "File not found"),

  // Email Errors (6000-6099)
  EMAIL_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 6000, "Failed to send email"),

  // AI/Image Service Errors (7000-7099)
  IMAGE_DOWNLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 7000, "Failed to download image"),
  IMAGE_SEARCH_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 7001, "Failed to search for image");

  private final HttpStatus httpStatus;
  private final int code;
  private final String defaultMessage;

  ErrorCode(HttpStatus httpStatus, int code, String defaultMessage) {
    this.httpStatus = httpStatus;
    this.code = code;
    this.defaultMessage = defaultMessage;
  }
}
