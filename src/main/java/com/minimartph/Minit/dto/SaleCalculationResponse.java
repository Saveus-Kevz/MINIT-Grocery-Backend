package com.minimartph.Minit.dto;

import java.math.BigDecimal;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaleCalculationResponse {
  private BigDecimal subtotal;
  private BigDecimal discountAmount;
  private BigDecimal vatAmount;
  private BigDecimal total;
  private String discountType;
  private List<SaleItemResponse> items; // ← add this field
}
