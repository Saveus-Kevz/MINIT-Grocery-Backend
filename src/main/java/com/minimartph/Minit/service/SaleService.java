package com.minimartph.Minit.service;

import com.minimartph.Minit.dto.*;
import com.minimartph.Minit.entity.*;
import com.minimartph.Minit.enums.SaleStatus;
import com.minimartph.Minit.exceptions.InsufficientStockException;
import com.minimartph.Minit.exceptions.InvalidCredentialsException;
import com.minimartph.Minit.exceptions.ResourceNotFoundException;
import com.minimartph.Minit.repository.*;
import jakarta.transaction.Transactional;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SaleService {

  private final SaleRepository saleRepository;
  private final ProductRepository productRepository;
  private final UserRepository userRepository;

  SaleService(SaleRepository saleRepository, ProductRepository productRepository, UserRepository userRepository) {
    this.saleRepository = saleRepository;
    this.productRepository = productRepository;
    this.userRepository = userRepository;
  }

  // ========== QUERY METHODS ==========

  public SaleResponse getSaleResponse(Long id) {
    Sale sale = getSale(id);
    return toSaleResponse(sale);
  }

  public List<SaleResponse> getSalesByDateRange(LocalDate from, LocalDate to) {
    LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDateTime.MIN;
    LocalDateTime end = (to != null) ? to.plusDays(1).atStartOfDay() : LocalDateTime.MAX;
    List<Sale> sales = saleRepository.findBySaleDateBetween(start, end);
    return sales.stream().map(this::toSaleResponse).collect(Collectors.toList());
  }

  public List<SaleResponse> getSalesByCashierAndDateRange(
      Long cashierId, LocalDate from, LocalDate to) {
    LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDateTime.MIN;
    LocalDateTime end = (to != null) ? to.plusDays(1).atStartOfDay() : LocalDateTime.MAX;
    List<Sale> sales = saleRepository.findByCashierIdAndSaleDateBetween(cashierId, start, end);
    return sales.stream().map(this::toSaleResponse).collect(Collectors.toList());
  }

  public List<DailySalesSummary> getTotalSalesByDate(LocalDate from, LocalDate to) {
    LocalDateTime start = (from != null) ? from.atStartOfDay() : LocalDateTime.MIN;
    LocalDateTime end = (to != null) ? to.plusDays(1).atStartOfDay() : LocalDateTime.MAX;
    List<Object[]> results = saleRepository.getTotalSalesByDateRangeNative(start, end);
    return results.stream()
        .map(
            row ->
                new DailySalesSummary(((java.sql.Date) row[0]).toLocalDate(), (BigDecimal) row[1]))
        .collect(Collectors.toList());
  }

  public Sale getSale(Long id) {
    return saleRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Sale", id));
  }

  public boolean hasSales(Long userId) {
    return saleRepository.existsByCashierId(userId);
  }

  // ========== COMMAND METHODS ==========

  // --- Calculation (no persistence) ---
  public SaleCalculationResponse calculateSale(SaleCalculationRequest request) {
    List<SaleItemResponse> items = new ArrayList<>();
    BigDecimal subtotal = BigDecimal.ZERO;

    for (SaleItemRequest itemReq : request.getItems()) {
      Product product =
          productRepository
              .findById(itemReq.getProductId())
              .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));
      BigDecimal itemSubtotal =
          product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
      subtotal = subtotal.add(itemSubtotal);

      items.add(
          SaleItemResponse.builder()
              .productId(product.getId())
              .productName(product.getName())
              .quantity(itemReq.getQuantity())
              .unitPrice(product.getPrice())
              .subtotal(itemSubtotal)
              .build());
    }

    BigDecimal discountAmount = BigDecimal.ZERO;
    BigDecimal vatAmount = BigDecimal.ZERO;
    BigDecimal total;
    String discountType = request.getDiscountType() != null ? request.getDiscountType() : "NONE";

    if ("PWD".equalsIgnoreCase(discountType)) {
      discountAmount = subtotal.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
      total = subtotal.subtract(discountAmount);
    } else {
      vatAmount = subtotal.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);
      total = subtotal.add(vatAmount);
    }

    return SaleCalculationResponse.builder()
        .subtotal(subtotal)
        .discountAmount(discountAmount)
        .vatAmount(vatAmount)
        .total(total)
        .discountType(discountType)
        .items(items)
        .build();
  }

  // --- Create sale with cashier from security context ---
  @Transactional
  public SaleResponse createSale(SaleRequest request) {
    // Get cashier from a security context
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new InvalidCredentialsException("Not authenticated");
    }

    User cashier = userRepository.findByUsernameAndActiveTrue(auth.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Cashier", auth.getName()));

    // Validate items and calculate subtotal
    BigDecimal subtotal = BigDecimal.ZERO;
    List<SaleItem> saleItems = new ArrayList<>();

    for (SaleItemRequest itemReq : request.getItems()) {
      Product product =
          productRepository
              .findById(itemReq.getProductId())
              .orElseThrow(() -> new ResourceNotFoundException("Product", itemReq.getProductId()));

      if (product.getStockQuantity() < itemReq.getQuantity()) {
        throw new InsufficientStockException(
            product.getName(), itemReq.getQuantity(), product.getStockQuantity());
      }

      BigDecimal itemSubtotal =
          product.getPrice().multiply(BigDecimal.valueOf(itemReq.getQuantity()));
      subtotal = subtotal.add(itemSubtotal);

      SaleItem si =
          SaleItem.builder()
              .product(product)
              .quantity(itemReq.getQuantity())
              .unitPrice(product.getPrice())
              .subtotal(itemSubtotal)
              .build();
      saleItems.add(si);
    }

    // Calculate discount, VAT, total
    BigDecimal discountAmount = BigDecimal.ZERO;
    BigDecimal vatAmount = BigDecimal.ZERO;
    BigDecimal total;
    String discountType = request.getDiscountType() != null ? request.getDiscountType() : "NONE";

    if ("PWD".equalsIgnoreCase(discountType)) {
      discountAmount = subtotal.multiply(new BigDecimal("0.20")).setScale(2, RoundingMode.HALF_UP);
      total = subtotal.subtract(discountAmount);
    } else {
      vatAmount = subtotal.multiply(new BigDecimal("0.12")).setScale(2, RoundingMode.HALF_UP);
      total = subtotal.add(vatAmount);
    }

    // Create and populate Sale entity
    Sale sale =
        Sale.builder()
            .saleDate(LocalDateTime.now())
            .subtotal(subtotal)
            .discountAmount(discountAmount)
            .vatAmount(vatAmount)
            .totalAmount(total)
            .discountType(discountType)
            .paymentMethod(request.getPaymentMethod())
            .cashier(cashier)
            .status(SaleStatus.COMPLETED)
            .build();

    // Link sale items back to sale
    for (SaleItem si : saleItems) {
      si.setSale(sale);
    }
    sale.setItems(saleItems);

    // Reduce stock
    for (SaleItem si : saleItems) {
      Product product = si.getProduct();
      product.setStockQuantity(product.getStockQuantity() - si.getQuantity());
      productRepository.save(product);
    }

    Sale savedSale = saleRepository.save(sale);

    return toSaleResponse(savedSale);
  }

  // --- Void sale with admin from security context ---
  @Transactional
  public void voidSale(Long saleId, String reason) {
    // Get admin from security context
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new InvalidCredentialsException("Not authenticated");
    }

    User admin =
        userRepository
            .findByUsernameAndActiveTrue(auth.getName())
            .orElseThrow(() -> new ResourceNotFoundException("Admin", auth.getName()));

    Sale sale =
        saleRepository
            .findById(saleId)
            .orElseThrow(() -> new ResourceNotFoundException("Sale", saleId));

    if (sale.getStatus() == SaleStatus.VOIDED) {
      throw new RuntimeException("Sale already voided");
    }

    // Restore stock for each item
    for (SaleItem item : sale.getItems()) {
      Product product = item.getProduct();
      product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
      productRepository.save(product);
    }

    sale.setStatus(SaleStatus.VOIDED);
    sale.setVoidReason(reason);
    sale.setVoidedAt(LocalDateTime.now());
    sale.setVoidedBy(admin.getId());
    saleRepository.save(sale);
  }

  // --- Helper to convert entity to response DTO ---
  public SaleResponse toSaleResponse(Sale sale) {
    List<SaleItemResponse> itemResponses =
        sale.getItems().stream()
            .map(
                item ->
                    SaleItemResponse.builder()
                        .productId(item.getProduct().getId())
                        .productName(item.getProduct().getName())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subtotal(item.getSubtotal())
                        .build())
            .collect(Collectors.toList());

    String cashierName =
        sale.getCashier() != null
            ? sale.getCashier().getFirstName() + " " + sale.getCashier().getLastName()
            : "Unknown";

    // Lambda version using array wrapper
    String[] voidedByAdminName = {null};
    if (sale.getVoidedBy() != null) {
      userRepository
          .findById(sale.getVoidedBy())
          .ifPresent(
              admin -> {
                voidedByAdminName[0] = admin.getFirstName() + " " + admin.getLastName();
              });
    }

    return SaleResponse.builder()
        .id(sale.getId())
        .saleDate(sale.getSaleDate())
        .subtotal(sale.getSubtotal())
        .discountAmount(sale.getDiscountAmount())
        .vatAmount(sale.getVatAmount())
        .totalAmount(sale.getTotalAmount())
        .discountType(sale.getDiscountType())
        .paymentMethod(sale.getPaymentMethod())
        .items(itemResponses)
        .cashierName(cashierName)
        .cashierActive(sale.getCashier() != null ? sale.getCashier().isActive() : false)
        .status(sale.getStatus())
        .voidReason(sale.getVoidReason())
        .voidedAt(sale.getVoidedAt())
        .voidedByAdminName(voidedByAdminName[0])
        .build();
  }
}
