package com.minimartph.Minit.dto;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaleItemResponse {
  private Long productId;
  private String productName;
  private int quantity;
  private BigDecimal unitPrice;
  private BigDecimal subtotal;
}
