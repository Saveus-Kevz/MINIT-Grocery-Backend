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
  private BigDecimal
      discountAmount; // PWD 20% discount (applied before VAT? Usually discount before VAT, but PWD

  // is VAT-exempt? In PH, PWD discount is 20% off the total amount, and the
  // discounted amount is VAT-exempt? Actually law: PWD gets 20% discount and
  // VAT exemption on the discounted amount. But for simplicity, we'll
  // implement: subtotal, then apply discount, then VAT on the discounted amount
  // if not exempt. However PWD is VAT-exempt, so final = subtotal - discount.
  // We'll handle with discountType.)

  @Column(nullable = false)
  private BigDecimal totalAmount; // final amount payable

  private String discountType; // "NONE", "PWD"

  @Enumerated(EnumType.STRING)
  @Column(length = 10, nullable = false)
  private PaymentMethod paymentMethod; // "CASH", "CARD", etc.

  @ManyToOne
  @JoinColumn(name = "cashier_id")
  private User cashier; // assuming User entity exists

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
