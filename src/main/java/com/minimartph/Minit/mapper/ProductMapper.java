package com.minimartph.Minit.mapper;

import com.minimartph.Minit.dto.ProductCreateRequest;
import com.minimartph.Minit.dto.ProductResponse;
import com.minimartph.Minit.dto.ProductUpdateRequest;
import com.minimartph.Minit.entity.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

  public Product toEntity(ProductCreateRequest request) {
    if (request == null) return null;
    Product product = new Product();
    product.setName(request.getName());
    product.setBarcode(request.getBarcode());
    product.setPrice(request.getPrice());
    product.setStockQuantity(request.getStockQuantity());
    product.setCategory(request.getCategory());
    return product;
  }

  public ProductResponse toResponse(Product product) {
    if (product == null) return null;
    ProductResponse response = new ProductResponse();
    response.setId(product.getId());
    response.setName(product.getName());
    response.setBarcode(product.getBarcode());
    response.setPrice(product.getPrice());
    response.setStockQuantity(product.getStockQuantity());
    response.setCategory(product.getCategory());
    response.setImageUrl(product.getImageUrl());
    response.setCreatedDateTime(product.getCreatedDateTime());
    return response;
  }

  // For partial update
  public void updateEntity(ProductUpdateRequest request, Product product) {
    if (request == null || product == null) return;
    if (request.getName() != null) product.setName(request.getName());
    if (request.getBarcode() != null) product.setBarcode(request.getBarcode());
    if (request.getPrice() != null) product.setPrice(request.getPrice());
    if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
    if (request.getCategory() != null) product.setCategory(request.getCategory());
    if (request.getImageUrl() != null) product.setImageUrl(request.getImageUrl());
  }
}
