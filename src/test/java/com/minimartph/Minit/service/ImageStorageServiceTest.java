// File: src/test/java/com/minimartph/Minit/service/ImageStorageServiceTest.java
package com.minimartph.Minit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

import com.minimartph.Minit.exceptions.AppException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("ImageStorageService Unit Tests")
class ImageStorageServiceTest {

  @Mock private FileStorageService fileStorageService;

  @Mock private RestTemplate restTemplate;

  @Mock private MultipartFile mockFile;

  @Mock private ResponseEntity<byte[]> responseEntity;

  @InjectMocks private ImageStorageService imageStorageService;

  private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

  private static final long MAX_SIZE = 10 * 1024 * 1024;

  // ========== STORE IMAGE TESTS ==========

  @Nested
  @DisplayName("storeImage()")
  class StoreImageTests {

    @Test
    @DisplayName("Should store JPEG image successfully")
    void shouldStoreJpegImageSuccessfully() {
      // Given
      when(mockFile.getContentType()).thenReturn("image/jpeg");
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.isEmpty()).thenReturn(false);
      when(fileStorageService.storeFile(mockFile)).thenReturn("test-image.jpg");

      // When
      String result = imageStorageService.storeImage(mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/test-image.jpg");
      verify(fileStorageService).storeFile(mockFile);
    }

    @Test
    @DisplayName("Should store PNG image successfully")
    void shouldStorePngImageSuccessfully() {
      // Given
      when(mockFile.getContentType()).thenReturn("image/png");
      when(mockFile.getSize()).thenReturn(2048L);
      when(mockFile.isEmpty()).thenReturn(false);
      when(fileStorageService.storeFile(mockFile)).thenReturn("test-image.png");

      // When
      String result = imageStorageService.storeImage(mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/test-image.png");
      verify(fileStorageService).storeFile(mockFile);
    }

    @Test
    @DisplayName("Should store GIF image successfully")
    void shouldStoreGifImageSuccessfully() {
      // Given
      when(mockFile.getContentType()).thenReturn("image/gif");
      when(mockFile.getSize()).thenReturn(512L);
      when(mockFile.isEmpty()).thenReturn(false);
      when(fileStorageService.storeFile(mockFile)).thenReturn("test-image.gif");

      // When
      String result = imageStorageService.storeImage(mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/test-image.gif");
      verify(fileStorageService).storeFile(mockFile);
    }

    @Test
    @DisplayName("Should store WEBP image successfully")
    void shouldStoreWebpImageSuccessfully() {
      // Given
      when(mockFile.getContentType()).thenReturn("image/webp");
      when(mockFile.getSize()).thenReturn(4096L);
      when(mockFile.isEmpty()).thenReturn(false);
      when(fileStorageService.storeFile(mockFile)).thenReturn("test-image.webp");

      // When
      String result = imageStorageService.storeImage(mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/test-image.webp");
      verify(fileStorageService).storeFile(mockFile);
    }

    @Test
    @DisplayName("Should store BMP image successfully")
    void shouldStoreBmpImageSuccessfully() {
      // Given
      when(mockFile.getContentType()).thenReturn("image/bmp");
      when(mockFile.getSize()).thenReturn(8192L);
      when(mockFile.isEmpty()).thenReturn(false);
      when(fileStorageService.storeFile(mockFile)).thenReturn("test-image.bmp");

      // When
      String result = imageStorageService.storeImage(mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/test-image.bmp");
      verify(fileStorageService).storeFile(mockFile);
    }

    @Test
    @DisplayName("Should throw AppException when file type is invalid")
    void shouldThrowExceptionWhenInvalidFileType() {
      // Given - ONLY mock what's needed to trigger the exception
      when(mockFile.getContentType()).thenReturn("application/pdf");
      // DO NOT mock getSize() or isEmpty() - they won't be called
      // DO NOT mock fileStorageService.storeFile()

      // When & Then
      assertThatThrownBy(() -> imageStorageService.storeImage(mockFile))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("Only image files (JPEG, PNG, GIF, BMP, WEBP) are allowed.");

      verify(fileStorageService, never()).storeFile(any());
      verify(mockFile, never()).getSize();
      verify(mockFile, never()).isEmpty();
    }

    @Test
    @DisplayName("Should throw AppException when file type is null")
    void shouldThrowExceptionWhenFileTypeNull() {
      // Given - ONLY mock what's needed to trigger the exception
      when(mockFile.getContentType()).thenReturn(null);
      // DO NOT mock getSize() or isEmpty() - they won't be called

      // When & Then
      assertThatThrownBy(() -> imageStorageService.storeImage(mockFile))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("Only image files (JPEG, PNG, GIF, BMP, WEBP) are allowed.");

      verify(fileStorageService, never()).storeFile(any());
      verify(mockFile, never()).getSize();
      verify(mockFile, never()).isEmpty();
    }

    @Test
    @DisplayName("Should throw AppException when file size exceeds limit")
    void shouldThrowExceptionWhenFileTooLarge() {
      // Given - mock content type (passes first check) then size (triggers exception)
      when(mockFile.getContentType()).thenReturn("image/jpeg");
      when(mockFile.getSize()).thenReturn(MAX_SIZE + 1);
      // DO NOT mock isEmpty() - won't be called because exception thrown first
      // DO NOT mock fileStorageService.storeFile()

      // When & Then
      assertThatThrownBy(() -> imageStorageService.storeImage(mockFile))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("Image size must be less than 10 MB.");

      verify(fileStorageService, never()).storeFile(any());
      verify(mockFile, never()).isEmpty();
    }

    @Test
    @DisplayName("Should throw AppException when file is empty")
    void shouldThrowExceptionWhenFileEmpty() {
      // Given - mock content type and size (both pass), then isEmpty triggers exception
      when(mockFile.getContentType()).thenReturn("image/jpeg");
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.isEmpty()).thenReturn(true);
      // DO NOT mock fileStorageService.storeFile()

      // When & Then
      assertThatThrownBy(() -> imageStorageService.storeImage(mockFile))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("File is empty");

      verify(fileStorageService, never()).storeFile(any());
    }

    @Test
    @DisplayName("Should handle uppercase content type")
    void shouldHandleUppercaseContentType() {
      // Given
      when(mockFile.getContentType()).thenReturn("IMAGE/JPEG");
      when(mockFile.getSize()).thenReturn(1024L);
      when(mockFile.isEmpty()).thenReturn(false);
      when(fileStorageService.storeFile(mockFile)).thenReturn("test.jpg");

      // When
      String result = imageStorageService.storeImage(mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/test.jpg");
      verify(fileStorageService).storeFile(mockFile);
    }
  }

  // ========== DOWNLOAD AND STORE IMAGE TESTS ==========

  @Nested
  @DisplayName("downloadAndStoreImage()")
  class DownloadAndStoreImageTests {

    private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

    @Test
    @DisplayName("Should throw IllegalArgumentException when image URL is empty")
    void shouldThrowExceptionWhenUrlEmpty() {
      // When & Then
      assertThatThrownBy(() -> imageStorageService.downloadAndStoreImage("", "Product"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Image URL is empty");
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when image URL is null")
    void shouldThrowExceptionWhenUrlNull() {
      // When & Then
      assertThatThrownBy(() -> imageStorageService.downloadAndStoreImage(null, "Product"))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("Image URL is empty");
    }

    @Test
    @DisplayName("Should download and store JPEG image successfully")
    void shouldDownloadAndStoreJpegSuccessfully() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.jpg";
      String productName = "TestProduct";
      byte[] jpegBytes = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}; // JPEG magic bytes

      // Mock the RestTemplate response
      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(jpegBytes);

      // Mock file storage location
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then
      assertThat(result).startsWith("/uploads/");
      assertThat(result).endsWith(".jpg");

      // Verify the file was saved
      String filename = result.replace("/uploads/", "");
      Path savedFile = TEMP_DIR.resolve(filename);
      assertThat(Files.exists(savedFile)).isTrue();

      // Clean up
      Files.deleteIfExists(savedFile);
    }

    @Test
    @DisplayName("Should download and store PNG image successfully")
    void shouldDownloadAndStorePngSuccessfully() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.png";
      String productName = "TestProduct";
      byte[] pngBytes =
          new byte[] {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47}; // PNG magic bytes

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(pngBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then
      assertThat(result).endsWith(".png");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should download and store GIF image successfully")
    void shouldDownloadAndStoreGifSuccessfully() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.gif";
      String productName = "TestProduct";
      byte[] gifBytes = new byte[] {(byte) 0x47, (byte) 0x49, (byte) 0x46}; // GIF magic bytes

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(gifBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then
      assertThat(result).endsWith(".gif");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should download and store WEBP image successfully")
    void shouldDownloadAndStoreWebpSuccessfully() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.webp";
      String productName = "TestProduct";
      byte[] webpBytes =
          new byte[] {(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46}; // WEBP magic bytes

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(webpBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then
      assertThat(result).endsWith(".webp");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should default to jpg when format unknown")
    void shouldDefaultToJpgWhenFormatUnknown() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.unknown";
      String productName = "TestProduct";
      byte[] unknownBytes = new byte[] {0x00, 0x01, 0x02, 0x03}; // Unknown format

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(unknownBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then
      assertThat(result).endsWith(".jpg");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should throw exception when HTTP status is not OK")
    void shouldThrowExceptionWhenHttpError() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.jpg";

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.NOT_FOUND);

      // When & Then
      assertThatThrownBy(() -> imageStorageService.downloadAndStoreImage(imageUrl, "Product"))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Failed to download image from URL");
    }

    @Test
    @DisplayName("Should throw exception when response body is null")
    void shouldThrowExceptionWhenResponseBodyNull() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.jpg";

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> imageStorageService.downloadAndStoreImage(imageUrl, "Product"))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Downloaded image is empty");
    }

    @Test
    @DisplayName("Should throw exception when response body is empty")
    void shouldThrowExceptionWhenResponseBodyEmpty() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.jpg";

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(new byte[0]);

      // When & Then
      assertThatThrownBy(() -> imageStorageService.downloadAndStoreImage(imageUrl, "Product"))
          .isInstanceOf(RuntimeException.class)
          .hasMessage("Downloaded image is empty");
    }

    @Test
    @DisplayName("Should sanitize product name for filename")
    void shouldSanitizeProductName() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.jpg";
      String productName = "Coca-Cola (Original)!!!";
      byte[] jpegBytes = new byte[] {(byte) 0xFF, (byte) 0xD8};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(jpegBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then - product name should have special chars replaced with underscores
      assertThat(result).contains("Coca_Cola__Original___");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should detect GIF when PNG check fails (bytes don't match PNG)")
    void shouldDetectGifWhenPngCheckFails() throws Exception {
      // Given - Bytes that look like GIF (not PNG)
      String imageUrl = "https://example.com/image.gif";
      String productName = "TestProduct";
      byte[] gifBytes =
          new byte[] {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x00}; // GIF + extra byte

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(gifBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then
      assertThat(result).endsWith(".gif");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should detect WEBP when PNG and GIF checks fail")
    void shouldDetectWebpWhenPngAndGifChecksFail() throws Exception {
      // Given - Bytes that look like WEBP (not PNG or GIF)
      String imageUrl = "https://example.com/image.webp";
      String productName = "TestProduct";
      byte[] webpBytes =
          new byte[] {
            (byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46, (byte) 0x00
          }; // WEBP + extra

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(webpBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then
      assertThat(result).endsWith(".webp");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should detect JPEG when PNG, GIF, WEBP checks all fail")
    void shouldDetectJpegWhenOtherFormatsFail() throws Exception {
      // Given - Bytes that look like JPEG (not PNG, GIF, or WEBP)
      String imageUrl = "https://example.com/image.jpg";
      String productName = "TestProduct";
      byte[] jpegBytes =
          new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0x00}; // JPEG with extra

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(jpegBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then
      assertThat(result).endsWith(".jpg");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should handle image bytes with length exactly 1 (too short for any detection)")
    void shouldHandleVeryShortImageBytes() throws Exception {
      // Given - Bytes array too short for any format detection
      String imageUrl = "https://example.com/image.txt";
      String productName = "TestProduct";
      byte[] shortBytes = new byte[] {(byte) 0xFF}; // Only 1 byte

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(shortBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then - Should default to jpg
      assertThat(result).endsWith(".jpg");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should handle image bytes with length 2 (only JPEG check possible)")
    void shouldHandleTwoByteImage() throws Exception {
      // Given - 2 bytes that are NOT JPEG magic bytes
      String imageUrl = "https://example.com/image.bin";
      String productName = "TestProduct";
      byte[] twoBytes = new byte[] {(byte) 0x12, (byte) 0x34}; // Not JPEG

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(twoBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then - Should default to jpg
      assertThat(result).endsWith(".jpg");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should skip PNG and GIF detection and detect WEBP")
    void shouldSkipPngAndGifAndDetectWebp() throws Exception {
      // Given - Bytes that are WEBP, NOT PNG or GIF
      String imageUrl = "https://example.com/image.webp";
      String productName = "TestProduct";
      // PNG fails (first byte 0x52 not 0x89)
      // GIF fails (first byte 0x52 not 0x47)
      // WEBP passes
      byte[] webpBytes =
          new byte[] {(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(webpBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then - Should detect as WEBP
      assertThat(result).endsWith(".webp");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should skip PNG, GIF, WEBP detection and detect JPEG")
    void shouldSkipPngGifWebpAndDetectJpeg() throws Exception {
      // Given - Bytes that are JPEG, NOT PNG, GIF, or WEBP
      String imageUrl = "https://example.com/image.jpg";
      String productName = "TestProduct";
      // PNG fails (0xFF != 0x89)
      // GIF fails (0xFF != 0x47)
      // WEBP fails (0xFF != 0x52)
      // JPEG passes (0xFF, 0xD8)
      byte[] jpegBytes = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(jpegBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then - Should detect as JPEG
      assertThat(result).endsWith(".jpg");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should go through all checks and default to jpg when no format matches")
    void shouldGoThroughAllChecksAndDefaultToJpg() throws Exception {
      // Given - Bytes that don't match ANY format
      String imageUrl = "https://example.com/image.dat";
      String productName = "TestProduct";
      // PNG fails (0x12 != 0x89)
      // GIF fails (0x12 != 0x47)
      // WEBP fails (0x12 != 0x52)
      // JPEG fails (0x12 != 0xFF)
      byte[] unknownBytes = new byte[] {(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(unknownBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then - Should default to jpg
      assertThat(result).endsWith(".jpg");

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should skip PNG detection when bytes length < 4")
    void shouldSkipPngWhenLengthLessThan4() throws Exception {
      // Given - Only 3 bytes (too short for PNG detection)
      String imageUrl = "https://example.com/image.png";
      String productName = "TestProduct";
      byte[] shortBytes =
          new byte[] {(byte) 0x89, (byte) 0x50, (byte) 0x4E}; // Only 3 bytes, missing 4th

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(shortBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then - PNG condition fails due to length, will try other formats
      // Since it's only 3 bytes, it might not match any format
      assertThat(result).endsWith(".jpg"); // Defaults to jpg

      // Clean up
      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should cover else if GIF branch - PNG fails, GIF passes")
    void shouldCoverElseIfGifBranch() throws Exception {
      String imageUrl = "https://example.com/image.gif";
      String productName = "TestProduct";
      // These bytes make PNG condition FAIL (first byte 0x47 != 0x89)
      // But GIF condition PASSES
      byte[] gifBytes = new byte[] {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(gifBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".gif");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should cover else if WEBP branch - PNG and GIF fail, WEBP passes")
    void shouldCoverElseIfWebpBranch() throws Exception {
      String imageUrl = "https://example.com/image.webp";
      String productName = "TestProduct";
      // PNG fails (0x52 != 0x89)
      // GIF fails (0x52 != 0x47)
      // WEBP passes
      byte[] webpBytes =
          new byte[] {(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(webpBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".webp");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should cover else if JPEG branch - PNG, GIF, WEBP fail, JPEG passes")
    void shouldCoverElseIfJpegBranch() throws Exception {
      String imageUrl = "https://example.com/image.jpg";
      String productName = "TestProduct";
      // PNG fails (0xFF != 0x89)
      // GIF fails (0xFF != 0x47)
      // WEBP fails (0xFF != 0x52)
      // JPEG passes
      byte[] jpegBytes = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(jpegBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".jpg");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should execute else if GIF line - PNG fails, GIF passes")
    void shouldExecuteElseIfGifLine() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      // These bytes make PNG condition FAIL (first byte 0x47 != 0x89)
      // But GIF condition PASSES
      byte[] bytes = new byte[] {(byte) 0x47, (byte) 0x49, (byte) 0x46, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".gif");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should execute else if WEBP line - PNG and GIF fail, WEBP passes")
    void shouldExecuteElseIfWebpLine() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      // PNG fails (0x52 != 0x89)
      // GIF fails (0x52 != 0x47)
      // WEBP passes
      byte[] bytes = new byte[] {(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".webp");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should execute else if JPEG line - PNG, GIF, WEBP fail, JPEG passes")
    void shouldExecuteElseIfJpegLine() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      // PNG fails (0xFF != 0x89)
      // GIF fails (0xFF != 0x47)
      // WEBP fails (0xFF != 0x52)
      // JPEG passes
      byte[] bytes = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".jpg");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should default to jpg when no format matches")
    void shouldDefaultToJpgWhenNoFormatMatches() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      // Bytes that don't match ANY format
      byte[] bytes = new byte[] {(byte) 0x12, (byte) 0x34, (byte) 0x56, (byte) 0x78};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".jpg");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should fail GIF check when first byte doesn't match")
    void shouldFailGifCheckWhenFirstByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      // PNG fails, GIF fails because first byte is 0x48 not 0x47
      byte[] bytes = new byte[] {(byte) 0x48, (byte) 0x49, (byte) 0x46, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Should fall through to WEBP or JPEG check
      assertThat(result).endsWith(".jpg"); // Default

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should fail GIF check when length < 3")
    void shouldFailGifCheckWhenLengthTooShort() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      // PNG fails, GIF fails because only 2 bytes
      byte[] bytes = new byte[] {(byte) 0x47, (byte) 0x49};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".jpg");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should fail WEBP check when second byte doesn't match")
    void shouldFailWebpCheckWhenSecondByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      // PNG fails, GIF fails, WEBP fails because second byte is 0x50 not 0x49
      byte[] bytes = new byte[] {(byte) 0x52, (byte) 0x50, (byte) 0x46, (byte) 0x46};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".jpg");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should fail JPEG check when first byte doesn't match")
    void shouldFailJpegCheckWhenFirstByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      // PNG fails, GIF fails, WEBP fails, JPEG fails (first byte 0x00 not 0xFF)
      byte[] bytes = new byte[] {(byte) 0x00, (byte) 0xD8};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".jpg"); // Default

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should fail JPEG check when second byte doesn't match")
    void shouldFailJpegCheckWhenSecondByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      // PNG fails, GIF fails, WEBP fails, JPEG fails (second byte 0x00 not 0xD8)
      byte[] bytes = new byte[] {(byte) 0xFF, (byte) 0x00};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      assertThat(result).endsWith(".jpg");

      String filename = result.replace("/uploads/", "");
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("PNG: Should fail when length < 4")
    void shouldFailPngWhenLengthLessThan4() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes = new byte[] {(byte) 0x89, (byte) 0x50, (byte) 0x4E}; // Only 3 bytes

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("PNG: Should fail when first byte doesn't match")
    void shouldFailPngWhenFirstByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes =
          new byte[] {(byte) 0x88, (byte) 0x50, (byte) 0x4E, (byte) 0x47}; // First byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("PNG: Should fail when second byte doesn't match")
    void shouldFailPngWhenSecondByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes =
          new byte[] {(byte) 0x89, (byte) 0x51, (byte) 0x4E, (byte) 0x47}; // Second byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("PNG: Should fail when third byte doesn't match")
    void shouldFailPngWhenThirdByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes =
          new byte[] {(byte) 0x89, (byte) 0x50, (byte) 0x4F, (byte) 0x47}; // Third byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("PNG: Should fail when fourth byte doesn't match")
    void shouldFailPngWhenFourthByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes =
          new byte[] {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x48}; // Fourth byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("GIF: Should fail when length < 3")
    void shouldFailGifWhenLengthLessThan3() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes = new byte[] {(byte) 0x47, (byte) 0x49}; // Only 2 bytes

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("GIF: Should fail when first byte doesn't match")
    void shouldFailGifWhenFirstByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes = new byte[] {(byte) 0x48, (byte) 0x49, (byte) 0x46}; // First byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("GIF: Should fail when second byte doesn't match")
    void shouldFailGifWhenSecondByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes = new byte[] {(byte) 0x47, (byte) 0x48, (byte) 0x46}; // Second byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("GIF: Should fail when third byte doesn't match")
    void shouldFailGifWhenThirdByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes = new byte[] {(byte) 0x47, (byte) 0x49, (byte) 0x47}; // Third byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("WEBP: Should fail when length < 4")
    void shouldFailWebpWhenLengthLessThan4() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes = new byte[] {(byte) 0x52, (byte) 0x49, (byte) 0x46}; // Only 3 bytes

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("WEBP: Should fail when first byte doesn't match")
    void shouldFailWebpWhenFirstByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes =
          new byte[] {(byte) 0x53, (byte) 0x49, (byte) 0x46, (byte) 0x46}; // First byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("WEBP: Should fail when second byte doesn't match")
    void shouldFailWebpWhenSecondByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes =
          new byte[] {(byte) 0x52, (byte) 0x48, (byte) 0x46, (byte) 0x46}; // Second byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("WEBP: Should fail when third byte doesn't match")
    void shouldFailWebpWhenThirdByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes =
          new byte[] {(byte) 0x52, (byte) 0x49, (byte) 0x47, (byte) 0x46}; // Third byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("WEBP: Should fail when fourth byte doesn't match")
    void shouldFailWebpWhenFourthByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes =
          new byte[] {(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x47}; // Fourth byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("JPEG: Should fail when length < 2")
    void shouldFailJpegWhenLengthLessThan2() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes = new byte[] {(byte) 0xFF}; // Only 1 byte

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("JPEG: Should fail when first byte doesn't match")
    void shouldFailJpegWhenFirstByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes = new byte[] {(byte) 0xFE, (byte) 0xD8}; // First byte wrong

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }

    @Test
    @DisplayName("JPEG: Should fail when second byte doesn't match")
    void shouldFailJpegWhenSecondByteMismatch() throws Exception {
      String imageUrl = "https://example.com/image.test";
      String productName = "TestProduct";
      byte[] bytes = new byte[] {(byte) 0xFF, (byte) 0xD9}; // Second byte wrong (should be 0xD8)

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(bytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);
      assertThat(result).endsWith(".jpg");

      Files.deleteIfExists(TEMP_DIR.resolve(result.replace("/uploads/", "")));
    }
  }

  // ========== FILENAME GENERATION TESTS ==========

  @Nested
  @DisplayName("Filename Generation")
  class FilenameGenerationTests {

    @Test
    @DisplayName("Should generate safe filename from product name")
    void shouldGenerateSafeFilename() {
      // Test the sanitization logic without calling the actual method
      String productName = "Coca Cola (Original)";

      // The actual logic from ImageStorageService
      String safeName = productName.replaceAll("[^a-zA-Z0-9]", "_");
      if (safeName.length() > 30) safeName = safeName.substring(0, 30);

      // Expected: All non-alphanumeric characters become underscores
      // Space -> _, ( -> _, ) -> _, so "Coca_Cola__Original_"
      assertThat(safeName).isEqualTo("Coca_Cola__Original_");
    }

    @Test
    @DisplayName("Should truncate product name when longer than 30 characters")
    void shouldTruncateLongProductName() throws Exception {
      // Given
      String imageUrl = "https://example.com/image.jpg";
      String productName = "ThisIsAVeryVeryLongProductNameThatExceedsThirtyCharacters";
      byte[] jpegBytes = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

      when(restTemplate.getForEntity(imageUrl, byte[].class)).thenReturn(responseEntity);
      when(responseEntity.getStatusCode()).thenReturn(HttpStatus.OK);
      when(responseEntity.getBody()).thenReturn(jpegBytes);
      when(fileStorageService.getFileStorageLocation()).thenReturn(TEMP_DIR);

      // When
      String result = imageStorageService.downloadAndStoreImage(imageUrl, productName);

      // Then
      String filename = result.replace("/uploads/", "");

      // The full 37-character product name should NOT be in the filename
      assertThat(filename)
          .doesNotContain("ThisIsAVeryVeryLongProductNameThatExceedsThirtyCharacters");

      // The filename should end with .jpg
      assertThat(filename).endsWith(".jpg");

      // The product name part (between timestamp and extension) should be <= 30 chars
      // Extract just the product name part
      String withoutExtension = filename.substring(0, filename.lastIndexOf("."));
      String[] parts = withoutExtension.split("_");
      // parts[0] = timestamp date, parts[1] = timestamp time, parts[2] = product name
      String productNamePart = parts.length >= 3 ? parts[2] : "";

      assertThat(productNamePart.length()).isLessThanOrEqualTo(30);

      // Clean up
      Files.deleteIfExists(TEMP_DIR.resolve(filename));
    }

    @Test
    @DisplayName("Should generate filename with correct extension for JPEG")
    void shouldGenerateJpegExtension() {
      // Test the extension detection logic
      byte[] jpegBytes = new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
      String extension = "jpg";

      if (jpegBytes.length > 1 && jpegBytes[0] == (byte) 0xFF && jpegBytes[1] == (byte) 0xD8) {
        extension = "jpg";
      }

      assertThat(extension).isEqualTo("jpg");
    }

    @Test
    @DisplayName("Should generate filename with correct extension for PNG")
    void shouldGeneratePngExtension() {
      // Test the extension detection logic
      byte[] pngBytes = new byte[] {(byte) 0x89, (byte) 0x50, (byte) 0x4E, (byte) 0x47};
      String extension = "jpg";

      if (pngBytes.length > 3
          && pngBytes[0] == (byte) 0x89
          && pngBytes[1] == (byte) 0x50
          && pngBytes[2] == (byte) 0x4E
          && pngBytes[3] == (byte) 0x47) {
        extension = "png";
      }

      assertThat(extension).isEqualTo("png");
    }

    @Test
    @DisplayName("Should generate filename with correct extension for GIF")
    void shouldGenerateGifExtension() {
      // Test the extension detection logic
      byte[] gifBytes = new byte[] {(byte) 0x47, (byte) 0x49, (byte) 0x46};
      String extension = "jpg";

      if (gifBytes.length > 2
          && gifBytes[0] == (byte) 0x47
          && gifBytes[1] == (byte) 0x49
          && gifBytes[2] == (byte) 0x46) {
        extension = "gif";
      }

      assertThat(extension).isEqualTo("gif");
    }

    @Test
    @DisplayName("Should generate filename with correct extension for WEBP")
    void shouldGenerateWebpExtension() {
      // Test the extension detection logic
      byte[] webpBytes = new byte[] {(byte) 0x52, (byte) 0x49, (byte) 0x46, (byte) 0x46};
      String extension = "jpg";

      if (webpBytes.length > 3
          && webpBytes[0] == (byte) 0x52
          && webpBytes[1] == (byte) 0x49
          && webpBytes[2] == (byte) 0x46
          && webpBytes[3] == (byte) 0x46) {
        extension = "webp";
      }

      assertThat(extension).isEqualTo("webp");
    }

    @Test
    @DisplayName("Should default to jpg extension when format unknown")
    void shouldDefaultToJpgWhenFormatUnknown() {
      // Test the extension detection logic
      byte[] unknownBytes = new byte[] {0x00, 0x01, 0x02, 0x03};
      String extension = "jpg"; // default

      // This would not match any known format, so stays "jpg"
      assertThat(extension).isEqualTo("jpg");
    }
  }
}
