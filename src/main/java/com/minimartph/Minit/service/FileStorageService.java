package com.minimartph.Minit.service;

import com.minimartph.Minit.repository.ProductRepository;
import com.minimartph.Minit.repository.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Service
public class FileStorageService {

  private final Path fileStorageLocation;

  public FileStorageService(
      @Value("${file.upload-dir}") String uploadDir,
      ProductRepository productRepository,
      UserRepository userRepository) {
    this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();

    System.out.println("FileStorageService base directory: " + this.fileStorageLocation);
    try {
      Files.createDirectories(this.fileStorageLocation);
    } catch (IOException e) {
      throw new RuntimeException("Could not create upload directory!", e);
    }
  }

  public String storeFile(MultipartFile file) {
    System.out.println("=== storeFile called ===");
    if (file.isEmpty()) {
      throw new RuntimeException("Failed to store empty file.");
    }

    try {
      String originalFilename = file.getOriginalFilename();
      String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
      String newFilename = timestamp + "_" + originalFilename;
      Path targetLocation = this.fileStorageLocation.resolve(newFilename);
      System.out.println("Full target path: " + targetLocation.toAbsolutePath());
      Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);
      System.out.println("File successfully written. Exists? " + Files.exists(targetLocation));
      System.out.println("File size: " + Files.size(targetLocation));
      return newFilename;
    } catch (IOException e) {
      e.printStackTrace();
      throw new RuntimeException("Failed to store file. Please try again!", e);
    }
  }

  public void deleteFile(String fileUrl) {
    if (fileUrl == null || fileUrl.isEmpty()) return;
    String fileName = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
    Path filePath = this.fileStorageLocation.resolve(fileName);
    try {
      Files.deleteIfExists(filePath);
    } catch (IOException e) {
      // Log error but don't throw – failing to delete an old file is not critical
      System.err.println("Failed to delete old file: " + filePath);
    }
  }

}
