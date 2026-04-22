package com.minimartph.Minit.service;

import com.minimartph.Minit.exceptions.AppException;
import com.minimartph.Minit.exceptions.ErrorCode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ImageStorageService {

  private static final Set<String> ALLOWED_IMAGE_TYPES =
      Set.of("image/jpeg", "image/png", "image/gif", "image/bmp", "image/webp");

  private static final long MAX_IMAGE_SIZE = 10 * 1024 * 1024; // 10 MB

  private final FileStorageService fileStorageService;
  private final RestTemplate restTemplate; // Add this

  @Autowired
  public ImageStorageService(FileStorageService fileStorageService, RestTemplate restTemplate) {
    this.fileStorageService = fileStorageService;
    this.restTemplate = restTemplate;
  }

  /**
   * Stores an image file after validating its content type and size. Returns the URL path (e.g.,
   * "/uploads/filename.jpg").
   */
  public String storeImage(MultipartFile file) {
    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
      throw new AppException(
          ErrorCode.INVALID_FILE_TYPE, "Only image files (JPEG, PNG, GIF, BMP, WEBP) are allowed.");
    }

    if (file.getSize() > MAX_IMAGE_SIZE) {
      throw new AppException(ErrorCode.FILE_TOO_LARGE, "Image size must be less than 10 MB.");
    }

    if (file.isEmpty()) {
      throw new AppException(ErrorCode.FILE_EMPTY, "File is empty");
    }

    String fileName = fileStorageService.storeFile(file);
    return "/uploads/" + fileName;
  }

  public String downloadAndStoreImage(String imageUrl, String productName) throws Exception {
    if (imageUrl == null || imageUrl.isEmpty()) {
      throw new IllegalArgumentException("Image URL is empty");
    }

    // Download the image
    ResponseEntity<byte[]> response = this.restTemplate.getForEntity(imageUrl, byte[].class);

    if (response.getStatusCode() != HttpStatus.OK) {
      throw new RuntimeException("Failed to download image from URL");
    }

    byte[] imageBytes = response.getBody();
    if (imageBytes == null || imageBytes.length == 0) {
      throw new RuntimeException("Downloaded image is empty");
    }

    // Detect image format from bytes
    String extension = "jpg"; // default

    // Check PNG (needs at least 4 bytes)
    if (imageBytes.length >= 4
        && imageBytes[0] == (byte) 0x89
        && imageBytes[1] == (byte) 0x50
        && imageBytes[2] == (byte) 0x4E
        && imageBytes[3] == (byte) 0x47) {
      extension = "png";
    }
    // Check GIF (needs at least 3 bytes)
    else if (imageBytes.length >= 3
        && imageBytes[0] == (byte) 0x47
        && imageBytes[1] == (byte) 0x49
        && imageBytes[2] == (byte) 0x46) {
      extension = "gif";
    }
    // Check WEBP (needs at least 4 bytes)
    else if (imageBytes.length >= 4
        && imageBytes[0] == (byte) 0x52
        && imageBytes[1] == (byte) 0x49
        && imageBytes[2] == (byte) 0x46
        && imageBytes[3] == (byte) 0x46) {
      extension = "webp";
    }
    // Check JPEG (needs at least 2 bytes)
    else if (imageBytes.length >= 2
        && imageBytes[0] == (byte) 0xFF
        && imageBytes[1] == (byte) 0xD8) {
      extension = "jpg";
    }

    // Generate filename with correct extension
    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
    String safeName = productName.replaceAll("[^a-zA-Z0-9]", "_");
    if (safeName.length() > 30) safeName = safeName.substring(0, 30);
    String filename = timestamp + "_" + safeName + "." + extension;

    // Save to disk
    Path targetLocation = fileStorageService.getFileStorageLocation().resolve(filename);
    Files.write(targetLocation, imageBytes);

    System.out.println("Downloaded AI image saved to: " + targetLocation);
    System.out.println("File extension detected: " + extension);

    return "/uploads/" + filename;
  }
}
