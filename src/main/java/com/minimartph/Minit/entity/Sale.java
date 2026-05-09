package com.minimartph.Minit.entity;

import com.minimartph.Minit.enums.PaymentMethod;
import com.minimartph.Minit.enums.SaleStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.*;

@Entity
@Table(name = "sales")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Sale {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private LocalDateTime saleDate;

  private BigDecimal subtotal; // before tax and discount
  private BigDecimal vatAmount; // 12% of taxable amount
  private BigDecimal discountAmount;

  @Column(nullable = false)
  private BigDecimal totalAmount; // final amount payable

  private String discountType; // "NONE", "PWD"

  @Enumerated(EnumType.STRING)
  @Column(length = 10, nullable = false)
  private PaymentMethod paymentMethod; // "CASH", "CARD", etc.

  @ManyToOne
  @JoinColumn(name = "cashier_id")
  private User cashier;  // ADMIN or CASHIER

  @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<SaleItem> items = new ArrayList<>();

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SaleStatus status = SaleStatus.COMPLETED;

  private String voidReason;
  private LocalDateTime voidedAt;
  private Long voidedBy;

  @PrePersist
  public void prePersist() {
    if (status == null) {
      status = SaleStatus.COMPLETED;
    }
  }
}
