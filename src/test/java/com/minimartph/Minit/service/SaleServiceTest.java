package com.minimartph.Minit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.minimartph.Minit.dto.*;
import com.minimartph.Minit.entity.*;
import com.minimartph.Minit.enums.PaymentMethod;
import com.minimartph.Minit.enums.Role;
import com.minimartph.Minit.enums.SaleStatus;
import com.minimartph.Minit.exceptions.InsufficientStockException;
import com.minimartph.Minit.exceptions.InvalidCredentialsException;
import com.minimartph.Minit.exceptions.ResourceNotFoundException;
import com.minimartph.Minit.repository.ProductRepository;
import com.minimartph.Minit.repository.SaleRepository;
import com.minimartph.Minit.repository.UserRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
@DisplayName("SaleService Unit Tests")
class SaleServiceTest {

  @Mock private SaleRepository saleRepository;

  @Mock private ProductRepository productRepository;

  @Mock private UserRepository userRepository;

  @Mock private Authentication authentication;

  @Mock private SecurityContext securityContext;

  @InjectMocks private SaleService saleService;

  private User sampleCashier;
  private User sampleAdmin;
  private Product sampleProduct;
  private Sale sampleSale;
  private SaleItem sampleSaleItem;
  private List<SaleItemRequest> sampleItemRequests;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);

    // Sample Cashier
    sampleCashier = new User();
    sampleCashier.setId(1L);
    sampleCashier.setUsername("cashier1");
    sampleCashier.setFirstName("John");
    sampleCashier.setLastName("Doe");
    sampleCashier.setRole(Role.CASHIER);
    sampleCashier.setActive(true);

    // Sample Admin
    sampleAdmin = new User();
    sampleAdmin.setId(2L);
    sampleAdmin.setUsername("admin1");
    sampleAdmin.setFirstName("Admin");
    sampleAdmin.setLastName("User");
    sampleAdmin.setRole(Role.ADMIN);
    sampleAdmin.setActive(true);

    // Sample Product
    sampleProduct = new Product();
    sampleProduct.setId(1L);
    sampleProduct.setName("Test Product");
    sampleProduct.setBarcode("TEST123");
    sampleProduct.setPrice(new BigDecimal("100.00"));
    sampleProduct.setStockQuantity(50);
    sampleProduct.setCategory("Beverages");

    // Sample SaleItem
    sampleSaleItem =
        SaleItem.builder()
            .id(1L)
            .product(sampleProduct)
            .quantity(2)
            .unitPrice(new BigDecimal("100.00"))
            .subtotal(new BigDecimal("200.00"))
            .build();

    // Sample Sale (no voidedBy set - so userRepository.findById won't be called)
    sampleSale =
        Sale.builder()
            .id(1L)
            .saleDate(LocalDateTime.now())
            .subtotal(new BigDecimal("200.00"))
            .discountAmount(BigDecimal.ZERO)
            .vatAmount(new BigDecimal("24.00"))
            .totalAmount(new BigDecimal("224.00"))
            .discountType("NONE")
            .paymentMethod(PaymentMethod.CASH)
            .cashier(sampleCashier)
            .status(SaleStatus.COMPLETED)
            .items(new ArrayList<>())
            .build();

    sampleSale.getItems().add(sampleSaleItem);
    sampleSaleItem.setSale(sampleSale);

    // Sample Item Requests
    SaleItemRequest itemRequest = new SaleItemRequest();
    itemRequest.setProductId(1L);
    itemRequest.setQuantity(2);
    sampleItemRequests = new ArrayList<>();
    sampleItemRequests.add(itemRequest);
  }

  // ========== QUERY METHODS TESTS ==========

  @Nested
  @DisplayName("getSaleResponse()")
  class GetSaleResponseTests {

    @Test
    @DisplayName("Should return sale response when sale exists")
    void shouldReturnSaleResponseWhenSaleExists() {
      // Given
      when(saleRepository.findById(1L)).thenReturn(Optional.of(sampleSale));
      // NO mock for userRepository.findById() because voidedBy is null

      // When
      SaleResponse result = saleService.getSaleResponse(1L);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("224.00"));
      assertThat(result.getCashierName()).isEqualTo("John Doe");
      assertThat(result.getStatus()).isEqualTo(SaleStatus.COMPLETED);
      verify(saleRepository).findById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when sale not found")
    void shouldThrowExceptionWhenSaleNotFound() {
      // Given
      when(saleRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> saleService.getSaleResponse(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Sale not found with id: 999");
    }
  }

  @Nested
  @DisplayName("getSalesByDateRange()")
  class GetSalesByDateRangeTests {

    @Test
    @DisplayName("Should return sales within date range")
    void shouldReturnSalesWithinDateRange() {
      // Given
      LocalDate from = LocalDate.of(2024, 1, 1);
      LocalDate to = LocalDate.of(2024, 1, 31);
      List<Sale> sales = new ArrayList<>();
      sales.add(sampleSale);
      when(saleRepository.findBySaleDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(sales);
      // NO mock for userRepository.findById() because voidedBy is null

      // When
      List<SaleResponse> result = saleService.getSalesByDateRange(from, to);

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getId()).isEqualTo(1L);
      verify(saleRepository)
          .findBySaleDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return all sales when date range is null")
    void shouldReturnAllSalesWhenDateRangeNull() {
      // Given
      List<Sale> sales = new ArrayList<>();
      sales.add(sampleSale);
      when(saleRepository.findBySaleDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(sales);
      // NO mock for userRepository.findById() because voidedBy is null

      // When
      List<SaleResponse> result = saleService.getSalesByDateRange(null, null);

      // Then
      assertThat(result).hasSize(1);
      verify(saleRepository)
          .findBySaleDateBetween(any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return empty list when no sales in date range")
    void shouldReturnEmptyListWhenNoSales() {
      // Given
      LocalDate from = LocalDate.of(2024, 1, 1);
      LocalDate to = LocalDate.of(2024, 1, 31);
      when(saleRepository.findBySaleDateBetween(any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(Collections.emptyList());

      // When
      List<SaleResponse> result = saleService.getSalesByDateRange(from, to);

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("getSalesByCashierAndDateRange()")
  class GetSalesByCashierAndDateRangeTests {

    @Test
    @DisplayName("Should return sales for specific cashier within date range")
    void shouldReturnSalesForCashierWithinDateRange() {
      // Given
      Long cashierId = 1L;
      LocalDate from = LocalDate.of(2024, 1, 1);
      LocalDate to = LocalDate.of(2024, 1, 31);
      List<Sale> sales = new ArrayList<>();
      sales.add(sampleSale);
      when(saleRepository.findByCashierIdAndSaleDateBetween(
              eq(cashierId), any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(sales);
      // NO mock for userRepository.findById() because voidedBy is null

      // When
      List<SaleResponse> result = saleService.getSalesByCashierAndDateRange(cashierId, from, to);

      // Then
      assertThat(result).hasSize(1);
      verify(saleRepository)
          .findByCashierIdAndSaleDateBetween(
              eq(cashierId), any(LocalDateTime.class), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return sales for cashier when 'from' date is null")
    void shouldReturnSalesForCashierWhenFromDateNull() {
      // Given
      Long cashierId = 1L;
      LocalDate to = LocalDate.of(2024, 1, 31);
      List<Sale> sales = new ArrayList<>();
      sales.add(sampleSale);
      when(saleRepository.findByCashierIdAndSaleDateBetween(
              eq(cashierId), any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(sales);

      // When
      List<SaleResponse> result = saleService.getSalesByCashierAndDateRange(cashierId, null, to);

      // Then
      assertThat(result).hasSize(1);
      verify(saleRepository)
          .findByCashierIdAndSaleDateBetween(
              eq(cashierId),
              argThat(start -> start.equals(LocalDateTime.MIN)),
              any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return sales for cashier when 'to' date is null")
    void shouldReturnSalesForCashierWhenToDateNull() {
      // Given
      Long cashierId = 1L;
      LocalDate from = LocalDate.of(2024, 1, 1);
      List<Sale> sales = new ArrayList<>();
      sales.add(sampleSale);
      when(saleRepository.findByCashierIdAndSaleDateBetween(
              eq(cashierId), any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(sales);

      // When
      List<SaleResponse> result = saleService.getSalesByCashierAndDateRange(cashierId, from, null);

      // Then
      assertThat(result).hasSize(1);
      verify(saleRepository)
          .findByCashierIdAndSaleDateBetween(
              eq(cashierId),
              any(LocalDateTime.class),
              argThat(end -> end.equals(LocalDateTime.MAX)));
    }

    @Test
    @DisplayName("Should return sales for cashier when both dates are null")
    void shouldReturnSalesForCashierWhenBothDatesNull() {
      // Given
      Long cashierId = 1L;
      List<Sale> sales = new ArrayList<>();
      sales.add(sampleSale);
      when(saleRepository.findByCashierIdAndSaleDateBetween(
              eq(cashierId), any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(sales);

      // When
      List<SaleResponse> result = saleService.getSalesByCashierAndDateRange(cashierId, null, null);

      // Then
      assertThat(result).hasSize(1);
      verify(saleRepository)
          .findByCashierIdAndSaleDateBetween(
              eq(cashierId),
              argThat(start -> start.equals(LocalDateTime.MIN)),
              argThat(end -> end.equals(LocalDateTime.MAX)));
    }
  }

  @Nested
  @DisplayName("getTotalSalesByDate()")
  class GetTotalSalesByDateTests {

    @Test
    @DisplayName("Should return daily sales summary")
    void shouldReturnDailySalesSummary() {
      // Given
      LocalDate from = LocalDate.of(2024, 1, 1);
      LocalDate to = LocalDate.of(2024, 1, 31);
      Object[] row = new Object[] {java.sql.Date.valueOf("2024-01-15"), new BigDecimal("500.00")};
      List<Object[]> results = new ArrayList<>();
      results.add(row);
      when(saleRepository.getTotalSalesByDateRangeNative(
              any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(results);

      // When
      List<DailySalesSummary> result = saleService.getTotalSalesByDate(from, to);

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getSaleDate()).isEqualTo(LocalDate.of(2024, 1, 15));
      assertThat(result.get(0).getTotalAmount()).isEqualTo(new BigDecimal("500.00"));
    }

    @Test
    @DisplayName("Should return daily sales summary when 'from' date is null")
    void shouldReturnDailySalesSummaryWhenFromDateNull() {
      // Given
      LocalDate to = LocalDate.of(2024, 1, 31);
      Object[] row = new Object[] {java.sql.Date.valueOf("2024-01-15"), new BigDecimal("500.00")};
      List<Object[]> results = new ArrayList<>();
      results.add(row);
      when(saleRepository.getTotalSalesByDateRangeNative(
              any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(results);

      // When
      List<DailySalesSummary> result = saleService.getTotalSalesByDate(null, to);

      // Then
      assertThat(result).hasSize(1);
      verify(saleRepository)
          .getTotalSalesByDateRangeNative(
              argThat(start -> start.equals(LocalDateTime.MIN)), any(LocalDateTime.class));
    }

    @Test
    @DisplayName("Should return daily sales summary when 'to' date is null")
    void shouldReturnDailySalesSummaryWhenToDateNull() {
      // Given
      LocalDate from = LocalDate.of(2024, 1, 1);
      Object[] row = new Object[] {java.sql.Date.valueOf("2024-01-15"), new BigDecimal("500.00")};
      List<Object[]> results = new ArrayList<>();
      results.add(row);
      when(saleRepository.getTotalSalesByDateRangeNative(
              any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(results);

      // When
      List<DailySalesSummary> result = saleService.getTotalSalesByDate(from, null);

      // Then
      assertThat(result).hasSize(1);
      verify(saleRepository)
          .getTotalSalesByDateRangeNative(
              any(LocalDateTime.class), argThat(end -> end.equals(LocalDateTime.MAX)));
    }

    @Test
    @DisplayName("Should return daily sales summary when both dates are null")
    void shouldReturnDailySalesSummaryWhenBothDatesNull() {
      // Given
      Object[] row = new Object[] {java.sql.Date.valueOf("2024-01-15"), new BigDecimal("500.00")};
      List<Object[]> results = new ArrayList<>();
      results.add(row);
      when(saleRepository.getTotalSalesByDateRangeNative(
              any(LocalDateTime.class), any(LocalDateTime.class)))
          .thenReturn(results);

      // When
      List<DailySalesSummary> result = saleService.getTotalSalesByDate(null, null);

      // Then
      assertThat(result).hasSize(1);
      verify(saleRepository)
          .getTotalSalesByDateRangeNative(
              argThat(start -> start.equals(LocalDateTime.MIN)),
              argThat(end -> end.equals(LocalDateTime.MAX)));
    }
  }

  @Nested
  @DisplayName("hasSales()")
  class HasSalesTests {

    @Test
    @DisplayName("Should return true when user has sales")
    void shouldReturnTrueWhenUserHasSales() {
      // Given
      when(saleRepository.existsByCashierId(1L)).thenReturn(true);

      // When
      boolean result = saleService.hasSales(1L);

      // Then
      assertThat(result).isTrue();
      verify(saleRepository).existsByCashierId(1L);
    }

    @Test
    @DisplayName("Should return false when user has no sales")
    void shouldReturnFalseWhenUserHasNoSales() {
      // Given
      when(saleRepository.existsByCashierId(999L)).thenReturn(false);

      // When
      boolean result = saleService.hasSales(999L);

      // Then
      assertThat(result).isFalse();
    }
  }

  // ========== COMMAND METHODS TESTS ==========

  @Nested
  @DisplayName("calculateSale()")
  class CalculateSaleTests {

    @Test
    @DisplayName("Should calculate sale correctly without discount")
    void shouldCalculateSaleWithoutDiscount() {
      // Given
      SaleCalculationRequest request = new SaleCalculationRequest();
      request.setItems(sampleItemRequests);
      request.setDiscountType("NONE");
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

      // When
      SaleCalculationResponse result = saleService.calculateSale(request);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getSubtotal()).isEqualTo(new BigDecimal("200.00"));
      assertThat(result.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
      assertThat(result.getVatAmount()).isEqualTo(new BigDecimal("24.00"));
      assertThat(result.getTotal()).isEqualTo(new BigDecimal("224.00"));
      assertThat(result.getDiscountType()).isEqualTo("NONE");
      assertThat(result.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("Should calculate sale correctly with PWD discount")
    void shouldCalculateSaleWithPWDDiscount() {
      // Given
      SaleCalculationRequest request = new SaleCalculationRequest();
      request.setItems(sampleItemRequests);
      request.setDiscountType("PWD");
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

      // When
      SaleCalculationResponse result = saleService.calculateSale(request);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getSubtotal()).isEqualTo(new BigDecimal("200.00"));
      assertThat(result.getDiscountAmount()).isEqualTo(new BigDecimal("40.00"));
      assertThat(result.getVatAmount()).isEqualTo(BigDecimal.ZERO);
      assertThat(result.getTotal()).isEqualTo(new BigDecimal("160.00"));
      assertThat(result.getDiscountType()).isEqualTo("PWD");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
      // Given
      SaleCalculationRequest request = new SaleCalculationRequest();
      request.setItems(sampleItemRequests);
      when(productRepository.findById(1L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> saleService.calculateSale(request))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Product not found with id: 1");
    }

    @Test
    @DisplayName("Should handle empty discount type as NONE")
    void shouldHandleEmptyDiscountType() {
      // Given
      SaleCalculationRequest request = new SaleCalculationRequest();
      request.setItems(sampleItemRequests);
      request.setDiscountType(null);
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

      // When
      SaleCalculationResponse result = saleService.calculateSale(request);

      // Then
      assertThat(result.getDiscountType()).isEqualTo("NONE");
      assertThat(result.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
    }
  }

  @Nested
  @DisplayName("createSale()")
  class CreateSaleTests {

    @Test
    @DisplayName("Should create sale successfully with valid data")
    void shouldCreateSaleSuccessfully() {
      // Given
      SaleRequest request = new SaleRequest();
      request.setItems(sampleItemRequests);
      request.setDiscountType("NONE");
      request.setPaymentMethod(PaymentMethod.CASH);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("cashier1");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("cashier1"))
          .thenReturn(Optional.of(sampleCashier));
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(saleRepository.save(any(Sale.class))).thenReturn(sampleSale);
      // NO mock for userRepository.findById() because voidedBy is null on the returned sale

      // When
      SaleResponse result = saleService.createSale(request);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getCashierName()).isEqualTo("John Doe");

      // Verify stock was reduced
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository, atLeastOnce()).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(48);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when not authenticated")
    void shouldThrowExceptionWhenNotAuthenticated() {
      // Given
      SaleRequest request = new SaleRequest();
      request.setItems(sampleItemRequests);

      when(securityContext.getAuthentication()).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> saleService.createSale(request))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessageContaining("Not authenticated");
    }

    @Test
    @DisplayName(
        "Should throw InvalidCredentialsException when authentication is not authenticated")
    void shouldThrowExceptionWhenAuthenticationNotAuthenticated() {
      // Given
      SaleRequest request = new SaleRequest();
      request.setItems(sampleItemRequests);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> saleService.createSale(request))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessageContaining("Not authenticated");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when cashier not found")
    void shouldThrowExceptionWhenCashierNotFound() {
      // Given
      SaleRequest request = new SaleRequest();
      request.setItems(sampleItemRequests);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("unknown");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("unknown")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> saleService.createSale(request))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Cashier not found with id: unknown");
    }

    @Test
    @DisplayName("Should throw InsufficientStockException when stock is insufficient")
    void shouldThrowExceptionWhenInsufficientStock() {
      // Given
      SaleRequest request = new SaleRequest();
      SaleItemRequest itemRequest = new SaleItemRequest();
      itemRequest.setProductId(1L);
      itemRequest.setQuantity(100);
      List<SaleItemRequest> items = new ArrayList<>();
      items.add(itemRequest);
      request.setItems(items);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("cashier1");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("cashier1"))
          .thenReturn(Optional.of(sampleCashier));
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

      // When & Then
      assertThatThrownBy(() -> saleService.createSale(request))
          .isInstanceOf(InsufficientStockException.class)
          .hasMessageContaining("Insufficient stock for product 'Test Product'");
    }

    @Test
    @DisplayName("Should apply PWD discount correctly when creating sale")
    void shouldApplyPWDDiscountCorrectly() {
      // Given
      SaleRequest request = new SaleRequest();
      request.setItems(sampleItemRequests);
      request.setDiscountType("PWD");
      request.setPaymentMethod(PaymentMethod.CASH);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("cashier1");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("cashier1"))
          .thenReturn(Optional.of(sampleCashier));
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

      // Create a sale with PWD discount that will be returned by save
      Sale pwdSale =
          Sale.builder()
              .id(2L)
              .saleDate(LocalDateTime.now())
              .subtotal(new BigDecimal("200.00"))
              .discountAmount(new BigDecimal("40.00"))
              .vatAmount(BigDecimal.ZERO)
              .totalAmount(new BigDecimal("160.00"))
              .discountType("PWD")
              .paymentMethod(PaymentMethod.CASH)
              .cashier(sampleCashier)
              .status(SaleStatus.COMPLETED)
              .items(new ArrayList<>())
              .build();

      SaleItem pwdItem =
          SaleItem.builder()
              .product(sampleProduct)
              .quantity(2)
              .unitPrice(new BigDecimal("100.00"))
              .subtotal(new BigDecimal("200.00"))
              .build();
      pwdItem.setSale(pwdSale);
      pwdSale.getItems().add(pwdItem);

      when(saleRepository.save(any(Sale.class))).thenReturn(pwdSale);
      // NO mock for userRepository.findById() because voidedBy is null

      // When
      SaleResponse result = saleService.createSale(request);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getDiscountAmount()).isEqualTo(new BigDecimal("40.00"));
      assertThat(result.getVatAmount()).isEqualTo(BigDecimal.ZERO);
      assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("160.00"));
      assertThat(result.getDiscountType()).isEqualTo("PWD");

      // Verify that the sale was saved with correct calculations
      ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
      verify(saleRepository).save(saleCaptor.capture());

      Sale capturedSale = saleCaptor.getValue();
      assertThat(capturedSale.getDiscountType()).isEqualTo("PWD");
      assertThat(capturedSale.getDiscountAmount()).isEqualTo(new BigDecimal("40.00"));
      assertThat(capturedSale.getTotalAmount()).isEqualTo(new BigDecimal("160.00"));
    }

    @Test
    @DisplayName("Should create sale with null discount type (defaults to NONE)")
    void shouldCreateSaleWithNullDiscountType() {
      // Given
      SaleRequest request = new SaleRequest();
      request.setItems(sampleItemRequests);
      request.setDiscountType(null); // Explicitly null
      request.setPaymentMethod(PaymentMethod.CASH);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("cashier1");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("cashier1"))
          .thenReturn(Optional.of(sampleCashier));
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(saleRepository.save(any(Sale.class))).thenReturn(sampleSale);

      // When
      SaleResponse result = saleService.createSale(request);

      // Then
      assertThat(result).isNotNull();

      // Verify the sale was created with default NONE discount
      ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
      verify(saleRepository).save(saleCaptor.capture());
      Sale capturedSale = saleCaptor.getValue();
      assertThat(capturedSale.getDiscountType()).isEqualTo("NONE");
      assertThat(capturedSale.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
      assertThat(capturedSale.getVatAmount()).isEqualTo(new BigDecimal("24.00"));
    }
  }

  @Nested
  @DisplayName("voidSale()")
  class VoidSaleTests {

    @Test
    @DisplayName("Should void sale successfully and restore stock")
    void shouldVoidSaleSuccessfullyAndRestoreStock() {
      // Given
      String reason = "Customer requested refund";

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin1");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin1"))
          .thenReturn(Optional.of(sampleAdmin));
      when(saleRepository.findById(1L)).thenReturn(Optional.of(sampleSale));

      // When
      saleService.voidSale(1L, reason);

      // Then
      ArgumentCaptor<Sale> saleCaptor = ArgumentCaptor.forClass(Sale.class);
      verify(saleRepository).save(saleCaptor.capture());

      Sale voidedSale = saleCaptor.getValue();
      assertThat(voidedSale.getStatus()).isEqualTo(SaleStatus.VOIDED);
      assertThat(voidedSale.getVoidReason()).isEqualTo(reason);
      assertThat(voidedSale.getVoidedAt()).isNotNull();
      assertThat(voidedSale.getVoidedBy()).isEqualTo(2L);

      // Verify stock was restored
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository, atLeastOnce()).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(52);
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException when admin not authenticated")
    void shouldThrowExceptionWhenAdminNotAuthenticated() {
      // Given
      when(securityContext.getAuthentication()).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> saleService.voidSale(1L, "Test reason"))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessageContaining("Not authenticated");
    }

    @Test
    @DisplayName(
        "Should throw InvalidCredentialsException when admin authentication is not authenticated")
    void shouldThrowExceptionWhenAdminAuthenticationNotAuthenticated() {
      // Given
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> saleService.voidSale(1L, "Test reason"))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessageContaining("Not authenticated");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when admin not found")
    void shouldThrowExceptionWhenAdminNotFound() {
      // Given
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("unknown");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("unknown")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> saleService.voidSale(1L, "Test reason"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Admin not found with id: unknown");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when sale not found")
    void shouldThrowExceptionWhenSaleNotFound() {
      // Given
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin1");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin1"))
          .thenReturn(Optional.of(sampleAdmin));
      when(saleRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> saleService.voidSale(999L, "Test reason"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Sale not found with id: 999");
    }

    @Test
    @DisplayName("Should throw RuntimeException when sale already voided")
    void shouldThrowExceptionWhenSaleAlreadyVoided() {
      // Given
      sampleSale.setStatus(SaleStatus.VOIDED);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin1");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin1"))
          .thenReturn(Optional.of(sampleAdmin));
      when(saleRepository.findById(1L)).thenReturn(Optional.of(sampleSale));

      // When & Then
      assertThatThrownBy(() -> saleService.voidSale(1L, "Test reason"))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Sale already voided");
    }
  }

  @Nested
  @DisplayName("toSaleResponse()")
  class ToSaleResponseTests {

    @Test
    @DisplayName("Should convert Sale entity to SaleResponse correctly")
    void shouldConvertToSaleResponseCorrectly() {
      // Given - NO mock for userRepository.findById() because voidedBy is null

      // When
      SaleResponse result = saleService.toSaleResponse(sampleSale);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
      assertThat(result.getSubtotal()).isEqualTo(new BigDecimal("200.00"));
      assertThat(result.getDiscountAmount()).isEqualTo(BigDecimal.ZERO);
      assertThat(result.getVatAmount()).isEqualTo(new BigDecimal("24.00"));
      assertThat(result.getTotalAmount()).isEqualTo(new BigDecimal("224.00"));
      assertThat(result.getDiscountType()).isEqualTo("NONE");
      assertThat(result.getPaymentMethod()).isEqualTo(PaymentMethod.CASH);
      assertThat(result.getCashierName()).isEqualTo("John Doe");
      assertThat(result.isCashierActive()).isTrue();
      assertThat(result.getStatus()).isEqualTo(SaleStatus.COMPLETED);
      assertThat(result.getItems()).hasSize(1);
      assertThat(result.getItems().get(0).getProductName()).isEqualTo("Test Product");
      assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
      assertThat(result.getItems().get(0).getUnitPrice()).isEqualTo(new BigDecimal("100.00"));
      assertThat(result.getItems().get(0).getSubtotal()).isEqualTo(new BigDecimal("200.00"));
    }

    @Test
    @DisplayName("Should handle sale with voidedBy admin name")
    void shouldHandleVoidedByAdminName() {
      // Given - This sale HAS voidedBy set, so we need to mock userRepository.findById
      Sale voidedSale =
          Sale.builder()
              .id(1L)
              .saleDate(LocalDateTime.now())
              .subtotal(new BigDecimal("200.00"))
              .discountAmount(BigDecimal.ZERO)
              .vatAmount(new BigDecimal("24.00"))
              .totalAmount(new BigDecimal("224.00"))
              .discountType("NONE")
              .paymentMethod(PaymentMethod.CASH)
              .cashier(sampleCashier)
              .status(SaleStatus.VOIDED)
              .voidReason("Test void reason")
              .voidedAt(LocalDateTime.now())
              .voidedBy(2L)
              .items(new ArrayList<>())
              .build();

      SaleItem item =
          SaleItem.builder()
              .product(sampleProduct)
              .quantity(2)
              .unitPrice(new BigDecimal("100.00"))
              .subtotal(new BigDecimal("200.00"))
              .build();
      item.setSale(voidedSale);
      voidedSale.getItems().add(item);

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleAdmin));

      // When
      SaleResponse result = saleService.toSaleResponse(voidedSale);

      // Then
      assertThat(result.getStatus()).isEqualTo(SaleStatus.VOIDED);
      assertThat(result.getVoidReason()).isEqualTo("Test void reason");
      assertThat(result.getVoidedAt()).isNotNull();
      assertThat(result.getVoidedByAdminName()).isEqualTo("Admin User");
    }

    @Test
    @DisplayName("Should handle cashier with inactive status")
    void shouldHandleInactiveCashier() {
      // Given
      User inactiveCashier = new User();
      inactiveCashier.setId(1L);
      inactiveCashier.setFirstName("John");
      inactiveCashier.setLastName("Doe");
      inactiveCashier.setRole(Role.CASHIER);
      inactiveCashier.setActive(false);

      Sale saleWithInactiveCashier =
          Sale.builder()
              .id(1L)
              .saleDate(LocalDateTime.now())
              .subtotal(new BigDecimal("200.00"))
              .discountAmount(BigDecimal.ZERO)
              .vatAmount(new BigDecimal("24.00"))
              .totalAmount(new BigDecimal("224.00"))
              .discountType("NONE")
              .paymentMethod(PaymentMethod.CASH)
              .cashier(inactiveCashier)
              .status(SaleStatus.COMPLETED)
              .items(new ArrayList<>())
              .build();

      SaleItem item =
          SaleItem.builder()
              .product(sampleProduct)
              .quantity(2)
              .unitPrice(new BigDecimal("100.00"))
              .subtotal(new BigDecimal("200.00"))
              .build();
      item.setSale(saleWithInactiveCashier);
      saleWithInactiveCashier.getItems().add(item);

      // NO mock for userRepository.findById() because voidedBy is null

      // When
      SaleResponse result = saleService.toSaleResponse(saleWithInactiveCashier);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getCashierName()).isEqualTo("John Doe");
      assertThat(result.isCashierActive()).isFalse();
    }

    @Test
    @DisplayName("Should handle null cashier")
    void shouldHandleNullCashier() {
      // Given
      Sale saleWithNullCashier =
          Sale.builder()
              .id(1L)
              .saleDate(LocalDateTime.now())
              .subtotal(new BigDecimal("200.00"))
              .discountAmount(BigDecimal.ZERO)
              .vatAmount(new BigDecimal("24.00"))
              .totalAmount(new BigDecimal("224.00"))
              .discountType("NONE")
              .paymentMethod(PaymentMethod.CASH)
              .cashier(null)
              .status(SaleStatus.COMPLETED)
              .items(new ArrayList<>())
              .build();

      SaleItem item =
          SaleItem.builder()
              .product(sampleProduct)
              .quantity(2)
              .unitPrice(new BigDecimal("100.00"))
              .subtotal(new BigDecimal("200.00"))
              .build();
      item.setSale(saleWithNullCashier);
      saleWithNullCashier.getItems().add(item);

      // NO mock for userRepository.findById() because voidedBy is null

      // When
      SaleResponse result = saleService.toSaleResponse(saleWithNullCashier);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getCashierName()).isEqualTo("Unknown");
      assertThat(result.isCashierActive()).isFalse();
    }
  }
}
