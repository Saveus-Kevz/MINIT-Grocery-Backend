package com.minimartph.Minit.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class SearchApiImageService {

  @Value("${searchapi.api.key}")
  private String apiKey;

  private static final String SEARCHAPI_URL = "https://www.searchapi.io/api/v1/search";

  private final RestTemplate restTemplate;

  SearchApiImageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  public String searchProductImage(String productName) {
    try {
      // Build the request URL
      String url =
          SEARCHAPI_URL + "?engine=google_images&q=" + productName.replace(" ", "+") + "&gl=ph";

      // Set up the HTTP headers with the Bearer token
      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", "Bearer " + apiKey);
      HttpEntity<String> entity = new HttpEntity<>(headers);

      // Make the API call
      ResponseEntity<String> response =
          restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

      // If the call is successful, parse the JSON to find the first image URL
      if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
        String body = response.getBody();
        // A simple search for the first occurrence of "original" and then the "link"
        int originalIndex = body.indexOf("\"original\"");
        if (originalIndex != -1) {
          int linkIndex = body.indexOf("\"link\"", originalIndex);
          if (linkIndex != -1) {
            int start = body.indexOf("\"", linkIndex + 7) + 1;
            int end = body.indexOf("\"", start);
            if (start > 0 && end > start) {
              return body.substring(start, end).replace("\\/", "/");
            }
          }
        }
      }
    } catch (Exception e) {
      System.err.println("SearchApi.io error: " + e.getMessage());
    }
    // Fallback to a placeholder image if the search fails
    return "https://placehold.co/400x400?text=" + productName.replace(" ", "+");
  }
}
