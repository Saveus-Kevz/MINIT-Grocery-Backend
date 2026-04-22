package com.minimartph.Minit.controller;

import com.minimartph.Minit.dto.SaleCalculationRequest;
import com.minimartph.Minit.dto.SaleCalculationResponse;
import com.minimartph.Minit.dto.SaleRequest;
import com.minimartph.Minit.dto.SaleResponse;
import com.minimartph.Minit.service.SaleService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
public class SaleController {

  @Autowired private SaleService saleService;

  @PostMapping("/calculate")
  public ResponseEntity<SaleCalculationResponse> calculate(
      @Valid @RequestBody SaleCalculationRequest request) {
    return ResponseEntity.ok(saleService.calculateSale(request));
  }

  @PostMapping
  public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleRequest request) {
    // Get cashierId from security context instead of hardcoded fallback
    SaleResponse response = saleService.createSale(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<SaleResponse> getSale(@PathVariable Long id) {
    return ResponseEntity.ok(saleService.getSaleResponse(id));
  }

  @GetMapping
  public ResponseEntity<List<SaleResponse>> getSalesByDate(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ResponseEntity.ok(saleService.getSalesByDateRange(from, to));
  }

  @GetMapping("/cashier")
  public ResponseEntity<List<SaleResponse>> getSalesByCashier(
      @RequestParam Long cashierId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return ResponseEntity.ok(saleService.getSalesByCashierAndDateRange(cashierId, from, to));
  }

  @PostMapping("/{id}/void")
  @PreAuthorize("hasRole('ADMIN')")
  public ResponseEntity<String> voidSale(
      @PathVariable Long id, @RequestBody(required = false) Map<String, String> payload) {
    String reason = payload != null ? payload.getOrDefault("reason", "") : "";
    saleService.voidSale(id, reason);
    return ResponseEntity.ok("Sale voided successfully");
  }
}
