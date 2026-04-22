package com.minimartph.Minit.dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DailySalesSummary {
  private LocalDate saleDate;
  private BigDecimal totalAmount;
}
