package com.minimartph.Minit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchApiImageService Unit Tests")
class SearchApiImageServiceTest {

  @Mock private RestTemplate restTemplate;

  @InjectMocks private SearchApiImageService searchApiImageService;

  private static final String API_KEY = "test-api-key";

  @BeforeEach
  void setUp() {
    ReflectionTestUtils.setField(searchApiImageService, "apiKey", API_KEY);
  }

  @Nested
  @DisplayName("searchProductImage()")
  class SearchProductImageTests {

    @Test
    @DisplayName("Should return image URL when API returns valid response")
    void shouldReturnImageUrlWhenApiReturnsValidResponse() {
      // Given
      String productName = "Coca Cola";
      String mockJsonResponse =
          "{\"images\":[{\"original\":\"https://example.com/cola.jpg\",\"link\":\"https://example.com/cola.jpg\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(mockJsonResponse, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).isEqualTo("https://example.com/cola.jpg");
      verify(restTemplate, times(1))
          .exchange(anyString(), any(HttpMethod.class), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should return placeholder when API returns 404")
    void shouldReturnPlaceholderWhenApiReturns404() {
      // Given
      String productName = "Test Product";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.NOT_FOUND);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
      verify(restTemplate, times(1))
          .exchange(anyString(), any(HttpMethod.class), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should return placeholder when API response body is null")
    void shouldReturnPlaceholderWhenResponseBodyNull() {
      // Given
      String productName = "Test Product";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
      verify(restTemplate, times(1))
          .exchange(anyString(), any(HttpMethod.class), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should return placeholder when JSON has no image")
    void shouldReturnPlaceholderWhenJsonHasNoImage() {
      // Given
      String productName = "Test Product";
      String mockJsonResponse = "{\"images\":[]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(mockJsonResponse, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
      verify(restTemplate, times(1))
          .exchange(anyString(), any(HttpMethod.class), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should return placeholder when JSON is malformed")
    void shouldReturnPlaceholderWhenJsonMalformed() {
      // Given
      String productName = "Test Product";
      String malformedJson = "{invalid json}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(malformedJson, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
      verify(restTemplate, times(1))
          .exchange(anyString(), any(HttpMethod.class), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should return placeholder when API throws exception")
    void shouldReturnPlaceholderWhenApiThrowsException() {
      // Given
      String productName = "Test Product";

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenThrow(new RestClientException("Network error"));

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
      verify(restTemplate, times(1))
          .exchange(anyString(), any(HttpMethod.class), any(), eq(String.class));
    }

    @Test
    @DisplayName("Should return placeholder when response has no original field")
    void shouldReturnPlaceholderWhenNoOriginalField() {
      // Given
      String productName = "Test Product";
      String jsonWithoutOriginal = "{\"images\":[{\"link\":\"https://example.com/image.jpg\"}]}";
      ResponseEntity<String> responseEntity =
          new ResponseEntity<>(jsonWithoutOriginal, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should return placeholder when response has no link field")
    void shouldReturnPlaceholderWhenNoLinkField() {
      // Given
      String productName = "Test Product";
      String jsonWithoutLink = "{\"images\":[{\"original\":\"https://example.com/image.jpg\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(jsonWithoutLink, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should return placeholder when original found but link not found")
    void shouldReturnPlaceholderWhenOriginalFoundButLinkNotFound() {
      // Given
      String productName = "Test Product";
      String json =
          "{\"images\":[{\"original\":\"https://example.com/image.jpg\",\"something\":\"value\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should return placeholder when link value cannot be parsed (invalid JSON format)")
    void shouldReturnPlaceholderWhenLinkValueCannotBeParsed() {
      // Given
      String productName = "Test Product";
      // Malformed JSON where link has no closing quote
      String json =
          "{\"images\":[{\"original\":\"https://example.com/image.jpg\",\"link\":\"https://example.com/image.jpg}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should handle escaped slashes in URL correctly")
    void shouldHandleEscapedSlashesInUrl() {
      // Given
      String productName = "Test Product";
      String json =
          "{\"images\":[{\"original\":\"https:\\/\\/example.com\\/image.jpg\",\"link\":\"https:\\/\\/example.com\\/image.jpg\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should return placeholder when API returns non-200 success status")
    void shouldReturnPlaceholderWhenApiReturnsNon200Success() {
      // Given
      String productName = "Test Product";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.CREATED); // 201

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should return placeholder when API returns 204 No Content")
    void shouldReturnPlaceholderWhenApiReturns204() {
      // Given
      String productName = "Test Product";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(HttpStatus.NO_CONTENT);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should return placeholder when API returns 500 error")
    void shouldReturnPlaceholderWhenApiReturns500() {
      // Given
      String productName = "Test Product";
      ResponseEntity<String> responseEntity =
          new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should handle product name with special characters")
    void shouldHandleProductNameWithSpecialCharacters() {
      // Given
      String productName = "Coca-Cola (Original) 100%!";
      String mockJsonResponse =
          "{\"images\":[{\"original\":\"https://example.com/cola.jpg\",\"link\":\"https://example.com/cola.jpg\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(mockJsonResponse, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).isEqualTo("https://example.com/cola.jpg");

      // Capture the URL and verify it contains the encoded product name
      ArgumentCaptor<String> urlCaptor = ArgumentCaptor.forClass(String.class);
      verify(restTemplate)
          .exchange(urlCaptor.capture(), any(HttpMethod.class), any(), eq(String.class));

      String capturedUrl = urlCaptor.getValue();
      // Just verify the URL was built with the product name (spaces become +)
      assertThat(capturedUrl).contains("q=Coca-Cola+(Original)+100%");
    }

    @Test
    @DisplayName("Should find the first original field when multiple exist")
    void shouldFindFirstOriginalFieldWhenMultipleExist() {
      // Given
      String productName = "Test Product";
      String json =
          "{\"images\":[{\"original\":\"https://example.com/first.jpg\",\"link\":\"https://example.com/first.jpg\"},{\"original\":\"https://example.com/second.jpg\",\"link\":\"https://example.com/second.jpg\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).isEqualTo("https://example.com/first.jpg");
    }

    @Test
    @DisplayName("Should find link after original even if link appears earlier in JSON")
    void shouldFindLinkAfterOriginalEvenIfLinkAppearsEarlier() {
      // Given
      String productName = "Test Product";
      String json =
          "{\"images\":[{\"link\":\"https://example.com/ignored.jpg\",\"original\":\"https://example.com/image.jpg\",\"link\":\"https://example.com/image.jpg\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).isEqualTo("https://example.com/image.jpg");
    }

    @Test
    @DisplayName("Should return placeholder when exception occurs during JSON parsing")
    void shouldReturnPlaceholderWhenExceptionDuringParsing() {
      // Given
      String productName = "Test Product";
      // This JSON will have start or end indices that are invalid
      String json = "{\"images\":[{\"original\":\"\",\"link\":\"\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should return placeholder when start is 0 (invalid)")
    void shouldReturnPlaceholderWhenStartIsZero() throws Exception {
      // Given - Create JSON where the link starts at position 0
      String productName = "Test Product";
      // The link is at the very beginning of the string
      String json = "\"link\":\"https://example.com/image.jpg\"}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should return placeholder when end is not greater than start")
    void shouldReturnPlaceholderWhenEndNotGreaterThanStart() throws Exception {
      // Given - Create JSON with malformed link where closing quote comes before opening quote
      String productName = "Test Product";
      // The link value is empty or malformed
      String json = "{\"images\":[{\"original\":\"https://example.com/image.jpg\",\"link\":\"\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should return placeholder when start is -1 (quote not found)")
    void shouldReturnPlaceholderWhenStartIsMinusOne() throws Exception {
      // Given - JSON with no quotes around the link value
      String productName = "Test Product";
      String json =
          "{\"images\":[{\"original\":\"https://example.com/image.jpg\",\"link\":https://example.com/image.jpg}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }

    @Test
    @DisplayName("Should return placeholder when end <= start")
    void shouldReturnPlaceholderWhenEndIsNotGreaterThanStart() throws Exception {
      // Given
      String productName = "Test Product";
      // Empty link value means start and end will be the same position
      String json = "{\"images\":[{\"original\":\"https://example.com/image.jpg\",\"link\":\"\"}]}";
      ResponseEntity<String> responseEntity = new ResponseEntity<>(json, HttpStatus.OK);

      when(restTemplate.exchange(anyString(), any(HttpMethod.class), any(), eq(String.class)))
          .thenReturn(responseEntity);

      // When
      String result = searchApiImageService.searchProductImage(productName);

      // Then
      assertThat(result).startsWith("https://placehold.co/400x400?text=Test+Product");
    }
  }
}
