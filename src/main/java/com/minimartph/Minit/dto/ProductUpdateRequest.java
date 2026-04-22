package com.minimartph.Minit.dto;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class ProductUpdateRequest {
  @Size(min = 1, max = 100, message = "Product name must be between 1 and 100 characters")
  private String name;

  @Size(max = 50, message = "Barcode cannot exceed 50 characters")
  private String barcode;

  @Positive(message = "Price must be greater than 0")
  @DecimalMax(value = "999999.99", message = "Price cannot exceed 999,999.99")
  private BigDecimal price;

  @PositiveOrZero(message = "Stock quantity cannot be negative")
  @Max(value = 999999, message = "Stock quantity cannot exceed 999,999")
  private Integer stockQuantity;

  @Size(max = 50, message = "Category cannot exceed 50 characters")
  private String category;

  @Size(max = 500, message = "Image URL cannot exceed 500 characters")
  private String imageUrl;
}
