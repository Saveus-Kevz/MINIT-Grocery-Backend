package com.minimartph.Minit.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class ProductResponse {
  private Long id;
  private String name;
  private String barcode;
  private BigDecimal price;
  private int stockQuantity;
  private String category;
  private String imageUrl;
  private LocalDateTime createdDateTime;
}
