package com.minimartph.Minit.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import lombok.*;

@Entity
@Table(name = "sale_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "sale_id")
  private Sale sale;

  @ManyToOne
  @JoinColumn(name = "product_id")
  private Product product;

  private int quantity;

  @Column(nullable = false)
  private BigDecimal unitPrice; // price at time of sale

  @Column(nullable = false)
  private BigDecimal subtotal;
}
