package com.minimartph.Minit.controller;

import com.minimartph.Minit.service.ImageStorageService;
import com.minimartph.Minit.service.SearchApiImageService;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
public class AIController {

  private final SearchApiImageService searchApiImageService;

  private final ImageStorageService imageStorageService;

  AIController(SearchApiImageService searchApiImageService, ImageStorageService imageStorageService) {
    this.searchApiImageService = searchApiImageService;
    this.imageStorageService = imageStorageService;
  }

  @PostMapping("/generate-image")
  public ResponseEntity<String> generateImage(@RequestBody Map<String, String> payload) {
    String productName = payload.get("productName");
    System.out.println("AI generate image requested for: " + productName);

    try {
      String externalImageUrl = searchApiImageService.searchProductImage(productName);
      System.out.println("External image URL: " + externalImageUrl);

      if (externalImageUrl == null || externalImageUrl.isEmpty()) {
        return ResponseEntity.badRequest().body("Failed to get image URL");
      }

      // This uses the NEW method - doesn't affect existing uploads
      String localImageUrl =
          imageStorageService.downloadAndStoreImage(externalImageUrl, productName);
      System.out.println("Saved to: " + localImageUrl);

      return ResponseEntity.ok(localImageUrl);
    } catch (Exception e) {
      System.err.println("AI generation failed: " + e.getMessage());
      e.printStackTrace();
      return ResponseEntity.internalServerError()
          .body("Failed to generate image: " + e.getMessage());
    }
  }
}
