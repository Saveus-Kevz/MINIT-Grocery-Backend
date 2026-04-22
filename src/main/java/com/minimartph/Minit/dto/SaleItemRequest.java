package com.minimartph.Minit.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class SaleItemRequest {
  @NotNull(message = "Product ID is required")
  private Long productId;

  @Positive(message = "Quantity must be at least 1")
  private int quantity;
}
