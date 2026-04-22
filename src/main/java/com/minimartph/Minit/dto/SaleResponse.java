package com.minimartph.Minit.dto;

import com.minimartph.Minit.enums.PaymentMethod;
import com.minimartph.Minit.enums.SaleStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SaleResponse {
  private Long id;
  private LocalDateTime saleDate;
  private BigDecimal subtotal;
  private BigDecimal discountAmount;
  private BigDecimal vatAmount;
  private BigDecimal totalAmount;
  private String discountType;
  private PaymentMethod paymentMethod;
  private List<SaleItemResponse> items;
  private String cashierName;
  private boolean cashierActive;
  private SaleStatus status;
  private String voidReason;
  private LocalDateTime voidedAt;
  private String voidedByAdminName;
}
