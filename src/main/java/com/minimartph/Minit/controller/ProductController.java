package com.minimartph.Minit.controller;

import com.minimartph.Minit.dto.ProductCreateRequest;
import com.minimartph.Minit.dto.ProductResponse;
import com.minimartph.Minit.dto.ProductUpdateRequest;
import com.minimartph.Minit.service.ImageStorageService;
import com.minimartph.Minit.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/products")
@Validated
public class ProductController {

  @Autowired private ProductService productService;

  @Autowired private ImageStorageService imageStorageService;

  @GetMapping
  public ResponseEntity<Page<ProductResponse>> getAllProducts(
      @RequestParam(defaultValue = "0") @Min(0) int page) {
    return ResponseEntity.ok(productService.getAllProducts(page));
  }

  @GetMapping("/category/{category}")
  public ResponseEntity<Page<ProductResponse>> getProductsByCategory(
      @PathVariable String category, @RequestParam(defaultValue = "0") @Min(0) int page) {
    return ResponseEntity.ok(productService.getProductsByCategory(category, page));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductResponse> getProductById(@PathVariable @Positive Long id) {
    return productService
        .getProductById(id)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/barcode/{barcode}")
  public ResponseEntity<ProductResponse> getProductByBarcode(@PathVariable String barcode) {
    return productService
        .getProductByBarcode(barcode)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/search")
  public ResponseEntity<List<ProductResponse>> searchProductByName(@RequestParam String name) {
    return ResponseEntity.ok(productService.searchProductsByName(name));
  }

  @PostMapping
  public ResponseEntity<ProductResponse> addProduct(
      @Valid @RequestBody ProductCreateRequest request) {
    ProductResponse created = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(created);
  }

  @PostMapping("/{id}/image")
  public ResponseEntity<String> uploadProductImage(
      @PathVariable Long id, @RequestParam("file") MultipartFile file) {

    String imageUrl = productService.uploadProductImage(id, file);
    return ResponseEntity.ok(imageUrl);
  }

  @PostMapping("/restock")
  public ResponseEntity<ProductResponse> restockProduct(
      @RequestParam String barcode, @RequestParam(defaultValue = "1") @Min(1) int quantity) {

    ProductResponse updated = productService.restockProduct(barcode, quantity);
    return ResponseEntity.ok(updated);
  }

  @PatchMapping("/{id}")
  public ResponseEntity<ProductResponse> updateProductPartial(
      @PathVariable Long id, @Valid @RequestBody ProductUpdateRequest updates) {

    ProductResponse updated = productService.updateProduct(id, updates);
    return ResponseEntity.ok(updated);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<?> deleteProduct(@PathVariable Long id) {
    try {
      productService.deleteProduct(id);
      return ResponseEntity.noContent().build();
    } catch (RuntimeException e) {
      return ResponseEntity.badRequest().body(e.getMessage());
    }
  }
}
