package com.minimartph.Minit.service;

import com.minimartph.Minit.dto.ProductCreateRequest;
import com.minimartph.Minit.dto.ProductResponse;
import com.minimartph.Minit.dto.ProductUpdateRequest;
import com.minimartph.Minit.entity.Product;
import com.minimartph.Minit.exceptions.DuplicateResourceException;
import com.minimartph.Minit.mapper.ProductMapper;
import com.minimartph.Minit.repository.ProductRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ProductService {

  @Autowired private ProductRepository productRepository;

  @Autowired private ProductMapper productMapper;

  @Autowired private ImageStorageService imageStorageService;

  @Autowired private FileStorageService fileStorageService;

  // ========== QUERY METHODS ==========

  public Page<ProductResponse> getAllProducts(int pageNumber) {
    Pageable pageable = PageRequest.of(pageNumber, 10);
    return productRepository.findAll(pageable).map(productMapper::toResponse);
  }

  public Page<ProductResponse> getProductsByCategory(String category, int pageNumber) {
    Pageable pageable = PageRequest.of(pageNumber, 10);
    return productRepository.findByCategory(category, pageable).map(productMapper::toResponse);
  }

  public Optional<ProductResponse> getProductById(Long id) {
    return productRepository.findById(id).map(productMapper::toResponse);
  }

  public Optional<ProductResponse> getProductByBarcode(String barcode) {
    return productRepository.findByBarcode(barcode).map(productMapper::toResponse);
  }

  public List<ProductResponse> searchProductsByName(String name) {
    return productRepository.findByNameContainingIgnoreCase(name).stream()
        .map(productMapper::toResponse)
        .collect(Collectors.toList());
  }

  // ========== COMMAND METHODS ==========

  @Transactional
  public ProductResponse createProduct(@NonNull ProductCreateRequest request) {
    if (productRepository.existsByBarcode(request.getBarcode())) {
      throw new DuplicateResourceException("Product", "barcode", request.getBarcode());
    }
    if (productRepository.existsByNameIgnoreCase(request.getName())) {
      throw new DuplicateResourceException("Product", "name", request.getName());
    }

    Product product = productMapper.toEntity(request);
    Product saved = productRepository.save(product);
    return productMapper.toResponse(saved);
  }

  @Transactional
  public String uploadProductImage(Long productId, MultipartFile file) {
    // Validate a file
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }

    // Find product
    Product product =
        productRepository
            .findById(productId)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

    // Store image
    String imageUrl = imageStorageService.storeImage(file);

    // Delete old image if exists
    if (product.getImageUrl() != null && product.getImageUrl().startsWith("/uploads/")) {
      fileStorageService.deleteFile(product.getImageUrl());
    }

    // Update product
    product.setImageUrl(imageUrl);
    productRepository.save(product);

    return imageUrl;
  }

  @Transactional
  public ProductResponse restockProduct(String barcode, int quantity) {
    Product product =
        productRepository
            .findByBarcode(barcode)
            .orElseThrow(() -> new RuntimeException("Product not found with barcode: " + barcode));

    if (quantity <= 0) {
      throw new RuntimeException("Restock quantity must be greater than 0");
    }

    product.setStockQuantity(product.getStockQuantity() + quantity);
    Product saved = productRepository.save(product);
    return productMapper.toResponse(saved);
  }

  @Transactional
  public ProductResponse updateProduct(Long id, ProductUpdateRequest updates) {
    Optional<Product> productOptional = productRepository.findById(id);
    if (productOptional.isEmpty()) {
      throw new RuntimeException("Product not found with id: " + id);
    }
    Product product = productOptional.get();

    // Validate name uniqueness if changed
    if (updates.getName() != null && !updates.getName().equals(product.getName())) {
      if (productRepository.existsByNameIgnoreCase(updates.getName())) {
        throw new RuntimeException("Product with name '" + updates.getName() + "' already exists");
      }
      product.setName(updates.getName());
    }

    // Validate barcode uniqueness if changed
    if (updates.getBarcode() != null && !updates.getBarcode().equals(product.getBarcode())) {
      if (productRepository.existsByBarcode(updates.getBarcode())) {
        throw new RuntimeException(
            "Product with barcode " + updates.getBarcode() + " already exists");
      }
      product.setBarcode(updates.getBarcode());
    }

    // Update price
    if (updates.getPrice() != null) {
      if (updates.getPrice().doubleValue() <= 0) {
        throw new RuntimeException("Price must be greater than 0");
      }
      product.setPrice(updates.getPrice());
    }

    // Update stock
    if (updates.getStockQuantity() != null) {
      if (updates.getStockQuantity() < 0) {
        throw new RuntimeException("Stock quantity cannot be negative");
      }
      product.setStockQuantity(updates.getStockQuantity());
    }

    // Update category
    if (updates.getCategory() != null) {
      product.setCategory(updates.getCategory());
    }

    // Handle image URL (deletion or update)
    if (updates.getImageUrl() != null) {
      if (updates.getImageUrl().isEmpty()) {
        // Delete existing image
        if (product.getImageUrl() != null && product.getImageUrl().startsWith("/uploads/")) {
          fileStorageService.deleteFile(product.getImageUrl());
        }
        product.setImageUrl(null);
      } else {
        // Update to a new image URL, delete old if different
        if (product.getImageUrl() != null && !product.getImageUrl().equals(updates.getImageUrl())) {
          if (product.getImageUrl().startsWith("/uploads/")) {
            fileStorageService.deleteFile(product.getImageUrl());
          }
        }
        product.setImageUrl(updates.getImageUrl());
      }
    }

    Product saved = productRepository.save(product);
    return productMapper.toResponse(saved);
  }

  @Transactional
  public void deleteProduct(Long id) {
    Product product =
        productRepository
            .findById(id)
            .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

    try {
      // Delete an associated image file if exists
      if (product.getImageUrl() != null && product.getImageUrl().startsWith("/uploads/")) {
        fileStorageService.deleteFile(product.getImageUrl());
      }
      productRepository.deleteById(id);
    } catch (DataIntegrityViolationException e) {
      throw new RuntimeException(
          "Cannot delete product with existing sales records. Mark as inactive instead.");
    }
  }
}
