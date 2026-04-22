package com.minimartph.Minit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.minimartph.Minit.dto.ProductCreateRequest;
import com.minimartph.Minit.dto.ProductResponse;
import com.minimartph.Minit.dto.ProductUpdateRequest;
import com.minimartph.Minit.entity.Product;
import com.minimartph.Minit.exceptions.DuplicateResourceException;
import com.minimartph.Minit.mapper.ProductMapper;
import com.minimartph.Minit.repository.ProductRepository;
import java.math.BigDecimal;
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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

  @Mock private ProductRepository productRepository;

  @Mock private ProductMapper productMapper;

  @Mock private ImageStorageService imageStorageService;

  @Mock private FileStorageService fileStorageService;

  @InjectMocks private ProductService productService;

  private Product sampleProduct;
  private ProductResponse sampleResponse;
  private ProductCreateRequest sampleCreateRequest;

  @BeforeEach
  void setUp() {
    sampleProduct = new Product();
    sampleProduct.setId(1L);
    sampleProduct.setName("Test Product");
    sampleProduct.setBarcode("TEST123");
    sampleProduct.setPrice(new BigDecimal("99.99"));
    sampleProduct.setStockQuantity(50);
    sampleProduct.setCategory("Beverages");
    sampleProduct.setImageUrl("/uploads/test.jpg");

    sampleResponse = new ProductResponse();
    sampleResponse.setId(1L);
    sampleResponse.setName("Test Product");
    sampleResponse.setBarcode("TEST123");
    sampleResponse.setPrice(new BigDecimal("99.99"));
    sampleResponse.setStockQuantity(50);
    sampleResponse.setCategory("Beverages");
    sampleResponse.setImageUrl("/uploads/test.jpg");

    sampleCreateRequest = new ProductCreateRequest();
    sampleCreateRequest.setName("Test Product");
    sampleCreateRequest.setBarcode("TEST123");
    sampleCreateRequest.setPrice(new BigDecimal("99.99"));
    sampleCreateRequest.setStockQuantity(50);
    sampleCreateRequest.setCategory("Beverages");
  }

  // ========== QUERY METHODS TESTS ==========

  @Nested
  @DisplayName("getAllProducts()")
  class GetAllProductsTests {

    @Test
    @DisplayName("Should return page of product responses when products exist")
    void shouldReturnPageOfProductResponses() {
      // Given
      int pageNumber = 0;
      Pageable pageable = PageRequest.of(pageNumber, 10);
      Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
      when(productRepository.findAll(pageable)).thenReturn(productPage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      Page<ProductResponse> result = productService.getAllProducts(pageNumber);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getName()).isEqualTo("Test Product");
      verify(productRepository).findAll(pageable);
      verify(productMapper).toResponse(sampleProduct);
    }

    @Test
    @DisplayName("Should return empty page when no products exist")
    void shouldReturnEmptyPageWhenNoProducts() {
      // Given
      int pageNumber = 0;
      Pageable pageable = PageRequest.of(pageNumber, 10);
      Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
      when(productRepository.findAll(pageable)).thenReturn(emptyPage);

      // When
      Page<ProductResponse> result = productService.getAllProducts(pageNumber);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).isEmpty();
      verify(productRepository).findAll(pageable);
      verify(productMapper, never()).toResponse(any());
    }
  }

  @Nested
  @DisplayName("getProductsByCategory()")
  class GetProductsByCategoryTests {

    @Test
    @DisplayName("Should return page of products by category when products exist")
    void shouldReturnPageOfProductsByCategory() {
      // Given
      String category = "Beverages";
      int pageNumber = 0;
      Pageable pageable = PageRequest.of(pageNumber, 10);
      Page<Product> productPage = new PageImpl<>(List.of(sampleProduct), pageable, 1);
      when(productRepository.findByCategory(category, pageable)).thenReturn(productPage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      Page<ProductResponse> result = productService.getProductsByCategory(category, pageNumber);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).hasSize(1);
      assertThat(result.getContent().get(0).getCategory()).isEqualTo("Beverages");
      verify(productRepository).findByCategory(category, pageable);
    }

    @Test
    @DisplayName("Should handle empty string category")
    void shouldHandleEmptyStringCategory() {
      // Given
      String category = "";
      int pageNumber = 0;
      Pageable pageable = PageRequest.of(pageNumber, 10);
      Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
      when(productRepository.findByCategory("", pageable)).thenReturn(emptyPage);

      // When
      Page<ProductResponse> result = productService.getProductsByCategory(category, pageNumber);

      // Then
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should return empty page when no products in category")
    void shouldReturnEmptyPageWhenNoProductsInCategory() {
      // Given
      String category = "EmptyCategory";
      int pageNumber = 0;
      Pageable pageable = PageRequest.of(pageNumber, 10);
      Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);
      when(productRepository.findByCategory(category, pageable)).thenReturn(emptyPage);

      // When
      Page<ProductResponse> result = productService.getProductsByCategory(category, pageNumber);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getContent()).isEmpty();
      verify(productRepository).findByCategory(category, pageable);
      verify(productMapper, never()).toResponse(any());
    }

    @Test
    @DisplayName("Should handle null category")
    void shouldHandleNullCategory() {
      // Given
      String category = null;
      int pageNumber = 0;
      Pageable pageable = PageRequest.of(pageNumber, 10);
      Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

      // Depending on your implementation, this might throw or return empty
      when(productRepository.findByCategory(null, pageable)).thenReturn(emptyPage);

      // When
      Page<ProductResponse> result = productService.getProductsByCategory(category, pageNumber);

      // Then
      assertThat(result).isNotNull();
    }
  }

  @Nested
  @DisplayName("getProductById()")
  class GetProductByIdTests {

    @Test
    @DisplayName("Should return product response when product exists")
    void shouldReturnProductResponseWhenExists() {
      // Given
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      Optional<ProductResponse> result = productService.getProductById(1L);

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getName()).isEqualTo("Test Product");
      verify(productRepository).findById(1L);
    }

    @Test
    @DisplayName("Should return empty optional when product not found")
    void shouldReturnEmptyOptionalWhenNotFound() {
      // Given
      when(productRepository.findById(999L)).thenReturn(Optional.empty());

      // When
      Optional<ProductResponse> result = productService.getProductById(999L);

      // Then
      assertThat(result).isEmpty();
      verify(productRepository).findById(999L);
      verify(productMapper, never()).toResponse(any());
    }
  }

  @Nested
  @DisplayName("getProductByBarcode()")
  class GetProductByBarcodeTests {

    @Test
    @DisplayName("Should return product response when barcode exists")
    void shouldReturnProductResponseWhenBarcodeExists() {
      // Given
      when(productRepository.findByBarcode("TEST123")).thenReturn(Optional.of(sampleProduct));
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      Optional<ProductResponse> result = productService.getProductByBarcode("TEST123");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getBarcode()).isEqualTo("TEST123");
      verify(productRepository).findByBarcode("TEST123");
    }

    @Test
    @DisplayName("Should return empty optional when barcode not found")
    void shouldReturnEmptyOptionalWhenBarcodeNotFound() {
      // Given
      when(productRepository.findByBarcode("NONEXISTENT")).thenReturn(Optional.empty());

      // When
      Optional<ProductResponse> result = productService.getProductByBarcode("NONEXISTENT");

      // Then
      assertThat(result).isEmpty();
      verify(productRepository).findByBarcode("NONEXISTENT");
    }
  }

  @Nested
  @DisplayName("searchProductsByName()")
  class SearchProductsByNameTests {

    @Test
    @DisplayName("Should return list of products matching name")
    void shouldReturnProductsMatchingName() {
      // Given
      List<Product> products = List.of(sampleProduct);
      when(productRepository.findByNameContainingIgnoreCase("Test")).thenReturn(products);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      List<ProductResponse> result = productService.searchProductsByName("Test");

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getName()).contains("Test");
      verify(productRepository).findByNameContainingIgnoreCase("Test");
    }

    @Test
    @DisplayName("Should return empty list when no products match")
    void shouldReturnEmptyListWhenNoMatches() {
      // Given
      when(productRepository.findByNameContainingIgnoreCase("xyz")).thenReturn(List.of());

      // When
      List<ProductResponse> result = productService.searchProductsByName("xyz");

      // Then
      assertThat(result).isEmpty();
      verify(productRepository).findByNameContainingIgnoreCase("xyz");
    }
  }

  // ========== COMMAND METHODS TESTS ==========

  @Nested
  @DisplayName("createProduct()")
  class CreateProductTests {

    @Test
    @DisplayName("Should create product successfully when valid data provided")
    void shouldCreateProductSuccessfully() {
      // Given
      when(productRepository.existsByBarcode("TEST123")).thenReturn(false);
      when(productRepository.existsByNameIgnoreCase("Test Product")).thenReturn(false);
      when(productMapper.toEntity(sampleCreateRequest)).thenReturn(sampleProduct);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      ProductResponse result = productService.createProduct(sampleCreateRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getName()).isEqualTo("Test Product");
      verify(productRepository).existsByBarcode("TEST123");
      verify(productRepository).existsByNameIgnoreCase("Test Product");
      verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when barcode already exists")
    void shouldThrowExceptionWhenBarcodeExists() {
      // Given
      when(productRepository.existsByBarcode("TEST123")).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> productService.createProduct(sampleCreateRequest))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("barcode")
          .hasMessageContaining("TEST123");

      verify(productRepository).existsByBarcode("TEST123");
      verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when name already exists")
    void shouldThrowExceptionWhenNameExists() {
      // Given
      when(productRepository.existsByBarcode("TEST123")).thenReturn(false);
      when(productRepository.existsByNameIgnoreCase("Test Product")).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> productService.createProduct(sampleCreateRequest))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("name")
          .hasMessageContaining("Test Product");

      verify(productRepository).existsByBarcode("TEST123");
      verify(productRepository).existsByNameIgnoreCase("Test Product");
      verify(productRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should create product successfully when barcode is null")
    void shouldCreateProductWhenBarcodeIsNull() {
      // Given
      sampleCreateRequest.setBarcode(null);

      when(productRepository.existsByBarcode(null)).thenReturn(false);
      when(productRepository.existsByNameIgnoreCase("Test Product")).thenReturn(false);
      when(productMapper.toEntity(sampleCreateRequest)).thenReturn(sampleProduct);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      ProductResponse result = productService.createProduct(sampleCreateRequest);

      // Then
      assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("Should handle barcode null in existsByBarcode properly")
    void shouldHandleNullBarcodeInExistsCheck() {
      // Given
      sampleCreateRequest.setBarcode(null);

      // Mock existsByBarcode with any() to handle null
      when(productRepository.existsByBarcode(any())).thenReturn(false);
      when(productRepository.existsByNameIgnoreCase("Test Product")).thenReturn(false);
      when(productMapper.toEntity(sampleCreateRequest)).thenReturn(sampleProduct);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      ProductResponse result = productService.createProduct(sampleCreateRequest);

      // Then
      assertThat(result).isNotNull();
      verify(productRepository).existsByBarcode(any());
    }
  }

  @Nested
  @DisplayName("uploadProductImage()")
  class UploadProductImageTests {

    private MultipartFile mockFile;

    @BeforeEach
    void setUp() {
      mockFile = mock(MultipartFile.class);
    }

    @Test
    @DisplayName("Should upload image successfully when product exists")
    void shouldUploadImageSuccessfully() throws Exception {
      // Given
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/new-image.jpg");
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

      // When
      String result = productService.uploadProductImage(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/new-image.jpg");
      verify(productRepository).findById(1L);
      verify(imageStorageService).storeImage(mockFile);
      verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName("Should delete old image when replacing existing image")
    void shouldDeleteOldImageWhenReplacing() throws Exception {
      // Given
      sampleProduct.setImageUrl("/uploads/old-image.jpg");
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/new-image.jpg");
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      doNothing().when(fileStorageService).deleteFile("/uploads/old-image.jpg");

      // When
      String result = productService.uploadProductImage(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/new-image.jpg");
      verify(fileStorageService).deleteFile("/uploads/old-image.jpg");
      verify(productRepository).save(sampleProduct);
    }

    @Test
    @DisplayName(
        "Should NOT delete when product has image that doesn't start with /uploads/ (covers line 92 branch)")
    void shouldNotDeleteWhenProductImageDoesNotStartWithUploads() throws Exception {
      // Given - Product with image URL that doesn't start with /uploads/
      Product productWithOtherImage = new Product();
      productWithOtherImage.setId(1L);
      productWithOtherImage.setName("Test Product");
      productWithOtherImage.setBarcode("TEST123");
      productWithOtherImage.setPrice(new BigDecimal("99.99"));
      productWithOtherImage.setStockQuantity(50);
      productWithOtherImage.setCategory("Beverages");
      productWithOtherImage.setImageUrl("data:image/png;base64,iVBORw0KGgo..."); // Base64 image

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithOtherImage));
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/new-image.jpg");
      when(productRepository.save(any(Product.class))).thenReturn(productWithOtherImage);

      // When
      String result = productService.uploadProductImage(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/new-image.jpg");
      // deleteFile should NOT be called because image doesn't start with /uploads/
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should NOT delete external image (http/https) when uploading")
    void shouldNotDeleteExternalHttpsImageWhenUploading() throws Exception {
      // Given - Product with HTTPS external image URL
      Product productWithHttpsImage = new Product();
      productWithHttpsImage.setId(1L);
      productWithHttpsImage.setName("Test Product");
      productWithHttpsImage.setBarcode("TEST123");
      productWithHttpsImage.setPrice(new BigDecimal("99.99"));
      productWithHttpsImage.setStockQuantity(50);
      productWithHttpsImage.setCategory("Beverages");
      productWithHttpsImage.setImageUrl("https://cloudinary.com/image.jpg"); // HTTPS external

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithHttpsImage));
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/new-image.jpg");
      when(productRepository.save(any(Product.class))).thenReturn(productWithHttpsImage);

      // When
      String result = productService.uploadProductImage(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/new-image.jpg");
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
      // Given
      when(productRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> productService.uploadProductImage(999L, mockFile))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    @DisplayName("Should throw exception when file is empty")
    void shouldThrowExceptionWhenFileEmpty() {
      // Given
      when(mockFile.isEmpty()).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> productService.uploadProductImage(1L, mockFile))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("File is empty");
    }

    @Test
    @DisplayName("Should throw exception when file is null")
    void shouldThrowExceptionWhenFileIsNull() {
      // When & Then
      assertThatThrownBy(() -> productService.uploadProductImage(1L, null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("File is empty");
    }

    @Test
    @DisplayName("Should upload image when product has external image (not delete external)")
    void shouldUploadImageWhenProductHasExternalImage() throws Exception {
      // Given - Product with EXTERNAL image URL
      Product productWithExternalImage = new Product();
      productWithExternalImage.setId(1L);
      productWithExternalImage.setName("Test Product");
      productWithExternalImage.setBarcode("TEST123");
      productWithExternalImage.setPrice(new BigDecimal("99.99"));
      productWithExternalImage.setStockQuantity(50);
      productWithExternalImage.setCategory("Beverages");
      productWithExternalImage.setImageUrl("https://external-cloud.com/old-image.jpg");

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithExternalImage));
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/new-image.jpg");
      when(productRepository.save(any(Product.class))).thenReturn(productWithExternalImage);

      // When
      String result = productService.uploadProductImage(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/new-image.jpg");
      // deleteFile should NOT be called because old image is external (doesn't start with
      // /uploads/)
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should NOT delete when product has external image URL (starts with http://)")
    void shouldNotDeleteWhenProductHasHttpExternalImage() throws Exception {
      // Given - Product with HTTP external image (not /uploads/)
      Product productWithHttpImage = new Product();
      productWithHttpImage.setId(1L);
      productWithHttpImage.setName("Test Product");
      productWithHttpImage.setBarcode("TEST123");
      productWithHttpImage.setPrice(new BigDecimal("99.99"));
      productWithHttpImage.setStockQuantity(50);
      productWithHttpImage.setCategory("Beverages");
      productWithHttpImage.setImageUrl("http://example.com/image.jpg"); // HTTP external

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithHttpImage));
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/new-image.jpg");
      when(productRepository.save(any(Product.class))).thenReturn(productWithHttpImage);

      // When
      String result = productService.uploadProductImage(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/new-image.jpg");
      // Verify deleteFile was NOT called because image is external (starts with http://)
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should NOT delete when product has external image URL (starts with https://)")
    void shouldNotDeleteWhenProductHasHttpsExternalImage() throws Exception {
      // Given - Product with HTTPS external image (not /uploads/)
      Product productWithHttpsImage = new Product();
      productWithHttpsImage.setId(1L);
      productWithHttpsImage.setName("Test Product");
      productWithHttpsImage.setBarcode("TEST123");
      productWithHttpsImage.setPrice(new BigDecimal("99.99"));
      productWithHttpsImage.setStockQuantity(50);
      productWithHttpsImage.setCategory("Beverages");
      productWithHttpsImage.setImageUrl("https://cloudinary.com/image.jpg"); // HTTPS external

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithHttpsImage));
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/new-image.jpg");
      when(productRepository.save(any(Product.class))).thenReturn(productWithHttpsImage);

      // When
      String result = productService.uploadProductImage(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/new-image.jpg");
      // Verify deleteFile was NOT called because image is external (starts with https://)
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should NOT delete external image when uploading new image")
    void shouldNotDeleteExternalImageWhenUploading() throws Exception {
      // Given - Product with EXTERNAL image URL (does NOT start with /uploads/)
      Product productWithExternalImage = new Product();
      productWithExternalImage.setId(1L);
      productWithExternalImage.setName("Test Product");
      productWithExternalImage.setBarcode("TEST123");
      productWithExternalImage.setPrice(new BigDecimal("99.99"));
      productWithExternalImage.setStockQuantity(50);
      productWithExternalImage.setCategory("Beverages");
      productWithExternalImage.setImageUrl("https://external-cloud.com/old-image.jpg");

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithExternalImage));
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/new-image.jpg");
      when(productRepository.save(any(Product.class))).thenReturn(productWithExternalImage);

      // When
      String result = productService.uploadProductImage(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/new-image.jpg");
      // Verify deleteFile was NOT called (because image is external)
      verify(fileStorageService, never()).deleteFile(anyString());
      verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should upload image when product has no existing image")
    void shouldUploadImageWhenProductHasNoExistingImage() throws Exception {
      // Given - Product with no existing image
      Product productWithoutImage = new Product();
      productWithoutImage.setId(1L);
      productWithoutImage.setName("Test Product");
      productWithoutImage.setBarcode("TEST123");
      productWithoutImage.setPrice(new BigDecimal("99.99"));
      productWithoutImage.setStockQuantity(50);
      productWithoutImage.setCategory("Beverages");
      productWithoutImage.setImageUrl(null); // No existing image

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithoutImage));
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/new-image.jpg");
      when(productRepository.save(any(Product.class))).thenReturn(productWithoutImage);

      // When
      String result = productService.uploadProductImage(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/new-image.jpg");
      // deleteFile should NOT be called because there's no existing image
      verify(fileStorageService, never()).deleteFile(anyString());
      verify(productRepository).save(productWithoutImage);
    }
  }

  @Nested
  @DisplayName("restockProduct()")
  class RestockProductTests {

    @Test
    @DisplayName("Should increase stock quantity when restocking")
    void shouldIncreaseStockQuantity() {
      // Given
      when(productRepository.findByBarcode("TEST123")).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      ProductResponse result = productService.restockProduct("TEST123", 10);

      // Then
      assertThat(result).isNotNull();
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(60); // 50 + 10
    }

    @Test
    @DisplayName("Should throw exception when product not found by barcode")
    void shouldThrowExceptionWhenProductNotFound() {
      // Given
      when(productRepository.findByBarcode("NONEXISTENT")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> productService.restockProduct("NONEXISTENT", 10))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Product not found with barcode: NONEXISTENT");
    }

    @Test
    @DisplayName("Should throw exception when quantity is zero or negative")
    void shouldThrowExceptionWhenQuantityInvalid() {
      // Given
      when(productRepository.findByBarcode("TEST123")).thenReturn(Optional.of(sampleProduct));

      // When & Then
      assertThatThrownBy(() -> productService.restockProduct("TEST123", 0))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Restock quantity must be greater than 0");

      assertThatThrownBy(() -> productService.restockProduct("TEST123", -5))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Restock quantity must be greater than 0");
    }
  }

  @Nested
  @DisplayName("updateProduct()")
  class UpdateProductTests {

    @Test
    @DisplayName("Should update product name successfully")
    void shouldUpdateProductName() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setName("Updated Name");

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.existsByNameIgnoreCase("Updated Name")).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      ProductResponse result = productService.updateProduct(1L, updateRequest);

      // Then
      assertThat(result).isNotNull();
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getName()).isEqualTo("Updated Name");
    }

    @Test
    @DisplayName(
        "Should NOT delete external image when updating to different URL (covers line 174-176 branch)")
    void shouldNotDeleteExternalImageWhenUpdatingToDifferentUrl_CoversBranch174() {
      // Given - Product with EXTERNAL image URL (does NOT start with /uploads/)
      Product productWithExternalImage = new Product();
      productWithExternalImage.setId(1L);
      productWithExternalImage.setName("Test Product");
      productWithExternalImage.setBarcode("TEST123");
      productWithExternalImage.setPrice(new BigDecimal("99.99"));
      productWithExternalImage.setStockQuantity(50);
      productWithExternalImage.setCategory("Beverages");
      productWithExternalImage.setImageUrl(
          "https://external-image.com/old.jpg"); // External, NOT /uploads/

      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("https://external-image.com/new.jpg"); // Different external URL

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithExternalImage));
      when(productRepository.save(any(Product.class))).thenReturn(productWithExternalImage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl())
          .isEqualTo("https://external-image.com/new.jpg");
      // deleteFile should NOT be called because old image is external (doesn't start with
      // /uploads/)
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should throw exception when updating to existing name")
    void shouldThrowExceptionWhenNameAlreadyExists() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setName("Existing Name");

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.existsByNameIgnoreCase("Existing Name")).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Product with name 'Existing Name' already exists");
    }

    @Test
    @DisplayName("Should update price successfully")
    void shouldUpdatePrice() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setPrice(new BigDecimal("149.99"));

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      ProductResponse result = productService.updateProduct(1L, updateRequest);

      // Then
      assertThat(result).isNotNull();
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getPrice()).isEqualTo(new BigDecimal("149.99"));
    }

    @Test
    @DisplayName("Should throw exception when price is zero or negative")
    void shouldThrowExceptionWhenPriceInvalid() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setPrice(new BigDecimal("0"));

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

      // When & Then
      assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Price must be greater than 0");
    }

    @Test
    @DisplayName("Should update category successfully")
    void shouldUpdateCategory() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setCategory("Snacks & Sweets");

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      ProductResponse result = productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getCategory()).isEqualTo("Snacks & Sweets");
    }

    @Test
    @DisplayName("Should delete image when imageUrl set to empty string")
    void shouldDeleteImageWhenImageUrlEmpty() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("");

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);
      doNothing().when(fileStorageService).deleteFile("/uploads/test.jpg");

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isNull();
      verify(fileStorageService).deleteFile("/uploads/test.jpg");
    }

    @Test
    @DisplayName("Should not update barcode when barcode is null (no change)")
    void shouldNotUpdateBarcodeWhenNull() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setName("Updated Name");
      // barcode is null - should not change

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.existsByNameIgnoreCase("Updated Name")).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getBarcode()).isEqualTo("TEST123");
      verify(productRepository, never()).existsByBarcode(anyString());
    }

    @Test
    @DisplayName("Should not update price when price is null (no change)")
    void shouldNotUpdatePriceWhenNull() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setName("Updated Name");
      // price is null - should not change

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.existsByNameIgnoreCase("Updated Name")).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should not update stock when stock is null (no change)")
    void shouldNotUpdateStockWhenNull() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setName("Updated Name");
      // stockQuantity is null - should not change

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.existsByNameIgnoreCase("Updated Name")).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(50);
    }

    @Test
    @DisplayName("Should update image URL with new value (not empty string)")
    void shouldUpdateImageUrlWithNewValue() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("/uploads/brand-new-image.jpg");

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isEqualTo("/uploads/brand-new-image.jpg");
      verify(fileStorageService).deleteFile("/uploads/test.jpg");
    }

    @Test
    @DisplayName("Should not delete anything when imageUrl is null (no change)")
    void shouldNotDeleteImageWhenImageUrlNull() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setName("Updated Name");
      // imageUrl is null - should not change

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.existsByNameIgnoreCase("Updated Name")).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isEqualTo("/uploads/test.jpg");
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should update barcode to new unique value")
    void shouldUpdateBarcodeToNewUniqueValue() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setBarcode("NEWBARCODE123");

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.existsByBarcode("NEWBARCODE123")).thenReturn(false);
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getBarcode()).isEqualTo("NEWBARCODE123");
    }

    @Test
    @DisplayName("Should throw exception when updating barcode to existing one")
    void shouldThrowExceptionWhenUpdatingToExistingBarcode() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setBarcode("EXISTING123");

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.existsByBarcode("EXISTING123")).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Product with barcode EXISTING123 already exists");
    }

    @Test
    @DisplayName("Should throw exception when stock quantity is negative")
    void shouldThrowExceptionWhenStockQuantityNegative() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setStockQuantity(-5);

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));

      // When & Then
      assertThatThrownBy(() -> productService.updateProduct(1L, updateRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Stock quantity cannot be negative");
    }

    @Test
    @DisplayName("Should not validate name when name is same as existing")
    void shouldNotValidateNameWhenSameAsExisting() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setName("Test Product"); // Same as existing name

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      // existsByNameIgnoreCase should NOT be called because name didn't change
      verify(productRepository, never()).existsByNameIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Should not validate barcode when barcode is same as existing")
    void shouldNotValidateBarcodeWhenSameAsExisting() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setBarcode("TEST123"); // Same as existing barcode

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      // existsByBarcode should NOT be called because barcode didn't change
      verify(productRepository, never()).existsByBarcode(anyString());
    }

    @Test
    @DisplayName("Should update image URL when product has no existing image")
    void shouldUpdateImageUrlWhenNoExistingImage() {
      // Given - Product with no existing image
      Product productWithoutImage = new Product();
      productWithoutImage.setId(1L);
      productWithoutImage.setName("Test Product");
      productWithoutImage.setBarcode("TEST123");
      productWithoutImage.setPrice(new BigDecimal("99.99"));
      productWithoutImage.setStockQuantity(50);
      productWithoutImage.setCategory("Beverages");
      productWithoutImage.setImageUrl(null); // No existing image

      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("/uploads/new-image.jpg");

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithoutImage));
      when(productRepository.save(any(Product.class))).thenReturn(productWithoutImage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isEqualTo("/uploads/new-image.jpg");
      // deleteFile should NOT be called because there's no existing image
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should update image URL when existing image is external (not /uploads/)")
    void shouldUpdateImageUrlWhenExistingImageIsExternal() {
      // Given - Product with external image URL (not managed by our system)
      Product productWithExternalImage = new Product();
      productWithExternalImage.setId(1L);
      productWithExternalImage.setName("Test Product");
      productWithExternalImage.setBarcode("TEST123");
      productWithExternalImage.setPrice(new BigDecimal("99.99"));
      productWithExternalImage.setStockQuantity(50);
      productWithExternalImage.setCategory("Beverages");
      productWithExternalImage.setImageUrl("https://external-image.com/old.jpg");

      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("/uploads/new-image.jpg");

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithExternalImage));
      when(productRepository.save(any(Product.class))).thenReturn(productWithExternalImage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isEqualTo("/uploads/new-image.jpg");
      // deleteFile should NOT be called because old image is external (doesn't start with
      // /uploads/)
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should not delete image when updating to same image URL")
    void shouldNotDeleteWhenUpdatingToSameImageUrl() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("/uploads/test.jpg"); // Same as existing

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isEqualTo("/uploads/test.jpg");
      // deleteFile should NOT be called because URL is the same
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName(
        "Should handle null image URL deletion when imageurl is empty string and product has no image")
    void shouldHandleDeleteImageWhenNoExistingImage() {
      // Given - Product with NO existing image
      Product productWithoutImage = new Product();
      productWithoutImage.setId(1L);
      productWithoutImage.setName("Test Product");
      productWithoutImage.setBarcode("TEST123");
      productWithoutImage.setPrice(new BigDecimal("99.99"));
      productWithoutImage.setStockQuantity(50);
      productWithoutImage.setCategory("Beverages");
      productWithoutImage.setImageUrl(null);

      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl(""); // Empty string to delete image

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithoutImage));
      when(productRepository.save(any(Product.class))).thenReturn(productWithoutImage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isNull();
      // deleteFile should NOT be called because there's no existing image
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should handle updating image URL when old image is null")
    void shouldUpdateImageUrlWhenOldImageIsNull() {
      // Given - Product with null image URL
      Product productWithNullImage = new Product();
      productWithNullImage.setId(1L);
      productWithNullImage.setName("Test Product");
      productWithNullImage.setBarcode("TEST123");
      productWithNullImage.setPrice(new BigDecimal("99.99"));
      productWithNullImage.setStockQuantity(50);
      productWithNullImage.setCategory("Beverages");
      productWithNullImage.setImageUrl(null);

      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("/uploads/new-image.jpg");

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithNullImage));
      when(productRepository.save(any(Product.class))).thenReturn(productWithNullImage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isEqualTo("/uploads/new-image.jpg");
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should handle all update scenarios with null values")
    void shouldHandleAllNullUpdateScenarios() {
      // Given - Update request with ALL null values (no changes)
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      // All fields are null - meaning no updates

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      ProductResponse result = productService.updateProduct(1L, updateRequest);

      // Then
      assertThat(result).isNotNull();
      // No validation methods should be called
      verify(productRepository, never()).existsByNameIgnoreCase(anyString());
      verify(productRepository, never()).existsByBarcode(anyString());
      verify(fileStorageService, never()).deleteFile(anyString());

      // Product should remain unchanged
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getName()).isEqualTo("Test Product");
      assertThat(productCaptor.getValue().getBarcode()).isEqualTo("TEST123");
      assertThat(productCaptor.getValue().getPrice()).isEqualTo(new BigDecimal("99.99"));
    }

    @Test
    @DisplayName("Should NOT delete external image URL when updating to new image")
    void shouldNotDeleteExternalImageUrlWhenUpdating() {
      // Given - Product with EXTERNAL image URL (not starting with /uploads/)
      Product productWithExternalImage = new Product();
      productWithExternalImage.setId(1L);
      productWithExternalImage.setName("Test Product");
      productWithExternalImage.setBarcode("TEST123");
      productWithExternalImage.setPrice(new BigDecimal("99.99"));
      productWithExternalImage.setStockQuantity(50);
      productWithExternalImage.setCategory("Beverages");
      productWithExternalImage.setImageUrl("https://external-cloud.com/image.jpg"); // External URL

      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("/uploads/new-image.jpg");

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithExternalImage));
      when(productRepository.save(any(Product.class))).thenReturn(productWithExternalImage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isEqualTo("/uploads/new-image.jpg");
      // deleteFile should NOT be called because old image is external
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should update stock quantity when value is valid")
    void shouldUpdateStockQuantityWhenValid() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setStockQuantity(75); // Valid positive stock

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getStockQuantity()).isEqualTo(75);
    }

    @Test
    @DisplayName("Should NOT delete image when updating to same image URL with /uploads/ prefix")
    void shouldNotDeleteWhenUpdatingToSameImageUrlWithUploadsPrefix() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("/uploads/test.jpg"); // Same as existing

      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);
      when(productMapper.toResponse(sampleProduct)).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
      verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should NOT delete external image when updating to different image URL")
    void shouldNotDeleteExternalImageWhenUpdatingToDifferentUrl() {
      // Given - Product with external image
      Product productWithExternalImage = new Product();
      productWithExternalImage.setId(1L);
      productWithExternalImage.setName("Test Product");
      productWithExternalImage.setBarcode("TEST123");
      productWithExternalImage.setPrice(new BigDecimal("99.99"));
      productWithExternalImage.setStockQuantity(50);
      productWithExternalImage.setCategory("Beverages");
      productWithExternalImage.setImageUrl("https://external-image.com/old.jpg"); // External

      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("/uploads/new-image.jpg"); // Different URL

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithExternalImage));
      when(productRepository.save(any(Product.class))).thenReturn(productWithExternalImage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isEqualTo("/uploads/new-image.jpg");
      // deleteFile should NOT be called because old image is external
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
      // Given
      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setName("Updated Name");

      when(productRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> productService.updateProduct(999L, updateRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    @DisplayName("Should update image URL to external URL when product has /uploads/ image")
    void shouldUpdateImageUrlToExternalUrl() {
      // Given - Product with /uploads/ image
      Product productWithInternalImage = new Product();
      productWithInternalImage.setId(1L);
      productWithInternalImage.setName("Test Product");
      productWithInternalImage.setBarcode("TEST123");
      productWithInternalImage.setPrice(new BigDecimal("99.99"));
      productWithInternalImage.setStockQuantity(50);
      productWithInternalImage.setCategory("Beverages");
      productWithInternalImage.setImageUrl("/uploads/old-image.jpg"); // Internal image

      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl("https://external-cloud.com/new-image.jpg"); // External URL

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithInternalImage));
      when(productRepository.save(any(Product.class))).thenReturn(productWithInternalImage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);
      doNothing().when(fileStorageService).deleteFile("/uploads/old-image.jpg");

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl())
          .isEqualTo("https://external-cloud.com/new-image.jpg");
      // deleteFile should be called because old image is internal (/uploads/)
      verify(fileStorageService).deleteFile("/uploads/old-image.jpg");
    }

    @Test
    @DisplayName("Should NOT delete when setting imageUrl to empty but product has external image")
    void shouldNotDeleteWhenSettingEmptyWithExternalImage() {
      // Given - Product with EXTERNAL image URL
      Product productWithExternalImage = new Product();
      productWithExternalImage.setId(1L);
      productWithExternalImage.setName("Test Product");
      productWithExternalImage.setBarcode("TEST123");
      productWithExternalImage.setPrice(new BigDecimal("99.99"));
      productWithExternalImage.setStockQuantity(50);
      productWithExternalImage.setCategory("Beverages");
      productWithExternalImage.setImageUrl("https://external-cloud.com/image.jpg"); // External

      ProductUpdateRequest updateRequest = new ProductUpdateRequest();
      updateRequest.setImageUrl(""); // Set to empty

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithExternalImage));
      when(productRepository.save(any(Product.class))).thenReturn(productWithExternalImage);
      when(productMapper.toResponse(any(Product.class))).thenReturn(sampleResponse);

      // When
      productService.updateProduct(1L, updateRequest);

      // Then
      ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
      verify(productRepository).save(productCaptor.capture());
      assertThat(productCaptor.getValue().getImageUrl()).isNull();
      // deleteFile should NOT be called because old image is external (doesn't start with
      // /uploads/)
      verify(fileStorageService, never()).deleteFile(anyString());
    }
  }

  @Nested
  @DisplayName("deleteProduct()")
  class DeleteProductTests {

    @Test
    @DisplayName("Should delete product successfully when no sales exist")
    void shouldDeleteProductSuccessfully() {
      // Given
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      doNothing().when(productRepository).deleteById(1L);
      doNothing().when(fileStorageService).deleteFile("/uploads/test.jpg");

      // When
      productService.deleteProduct(1L);

      // Then
      verify(productRepository).findById(1L);
      verify(fileStorageService).deleteFile("/uploads/test.jpg");
      verify(productRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Should delete product with external image URL (covers startsWith false branch)")
    void shouldDeleteProductWithExternalImageUrl_CoversBranch() {
      // Given - Product with external image URL that doesn't start with /uploads/
      Product productWithExternalImage = new Product();
      productWithExternalImage.setId(1L);
      productWithExternalImage.setName("External Image Product");
      productWithExternalImage.setBarcode("EXTIMAGE123");
      productWithExternalImage.setPrice(new BigDecimal("10.00"));
      productWithExternalImage.setStockQuantity(10);
      productWithExternalImage.setCategory("Test");
      productWithExternalImage.setImageUrl("https://example.com/image.jpg"); // External URL

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithExternalImage));
      doNothing().when(productRepository).deleteById(1L);

      // When
      productService.deleteProduct(1L);

      // Then
      verify(productRepository).deleteById(1L);
      // deleteFile should NOT be called because image is external
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should delete product with empty string image URL")
    void shouldDeleteProductWithEmptyStringImageUrl() {
      // Given - Product with empty string image URL
      Product productWithEmptyImage = new Product();
      productWithEmptyImage.setId(1L);
      productWithEmptyImage.setName("Empty Image Product");
      productWithEmptyImage.setBarcode("EMPTYIMAGE123");
      productWithEmptyImage.setPrice(new BigDecimal("10.00"));
      productWithEmptyImage.setStockQuantity(10);
      productWithEmptyImage.setCategory("Test");
      productWithEmptyImage.setImageUrl(""); // Empty string, not null

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithEmptyImage));
      doNothing().when(productRepository).deleteById(1L);

      // When
      productService.deleteProduct(1L);

      // Then
      verify(productRepository).deleteById(1L);
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should delete product successfully when product has no image")
    void shouldDeleteProductWithNoImage() {
      // Given
      Product productWithoutImage = new Product();
      productWithoutImage.setId(1L);
      productWithoutImage.setName("No Image Product");
      productWithoutImage.setBarcode("NOIMAGE123");
      productWithoutImage.setPrice(new BigDecimal("10.00"));
      productWithoutImage.setStockQuantity(10);
      productWithoutImage.setCategory("Test");
      productWithoutImage.setImageUrl(null);

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithoutImage));
      doNothing().when(productRepository).deleteById(1L);

      // When
      productService.deleteProduct(1L);

      // Then
      verify(productRepository).deleteById(1L);
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should delete product successfully when image is external URL (not /uploads/)")
    void shouldDeleteProductWithExternalImageUrl_NotUploads() {
      // Given
      Product productWithExternalImage = new Product();
      productWithExternalImage.setId(1L);
      productWithExternalImage.setName("External Image Product");
      productWithExternalImage.setBarcode("EXTIMAGE123");
      productWithExternalImage.setPrice(new BigDecimal("10.00"));
      productWithExternalImage.setStockQuantity(10);
      productWithExternalImage.setCategory("Test");
      productWithExternalImage.setImageUrl("https://example.com/image.jpg");

      when(productRepository.findById(1L)).thenReturn(Optional.of(productWithExternalImage));
      doNothing().when(productRepository).deleteById(1L);

      // When
      productService.deleteProduct(1L);

      // Then
      verify(productRepository).deleteById(1L);
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should throw exception when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
      // Given
      when(productRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> productService.deleteProduct(999L))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    @DisplayName("Should throw exception when product has sales records")
    void shouldThrowExceptionWhenProductHasSales() {
      // Given
      when(productRepository.findById(1L)).thenReturn(Optional.of(sampleProduct));
      doThrow(new DataIntegrityViolationException("Foreign key constraint"))
          .when(productRepository)
          .deleteById(1L);

      // When & Then
      assertThatThrownBy(() -> productService.deleteProduct(1L))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("Cannot delete product with existing sales records");
    }
  }
}
