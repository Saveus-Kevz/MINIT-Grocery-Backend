package com.minimartph.Minit.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.List;
import lombok.Data;

@Data
public class SaleCalculationRequest {
  @NotNull(message = "Items list cannot be null")
  private List<@Valid SaleItemRequest> items;

  @Pattern(regexp = "NONE|PWD", message = "Discount type must be NONE or PWD")
  private String discountType;
}
