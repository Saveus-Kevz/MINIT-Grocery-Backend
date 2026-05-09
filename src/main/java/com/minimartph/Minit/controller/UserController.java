package com.minimartph.Minit.controller;

import com.minimartph.Minit.dto.ChangePasswordRequest;
import com.minimartph.Minit.dto.UserRegistrationRequest;
import com.minimartph.Minit.dto.UserResponse;
import com.minimartph.Minit.dto.UserUpdateRequest;
import com.minimartph.Minit.entity.User;
import com.minimartph.Minit.mapper.UserMapper;
import com.minimartph.Minit.service.UserService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/users")
public class UserController {

  private final UserService userService;

  private final UserMapper userMapper;

  UserController(UserService userService, UserMapper userMapper) {
    this.userService = userService;
    this.userMapper = userMapper;
  }

  @PostMapping("/register")
  public ResponseEntity<UserResponse> registerStaff(
      @Valid @RequestBody UserRegistrationRequest request) {
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(userMapper.toResponse(userService.registerUser(request)));
  }

  @GetMapping
  public ResponseEntity<List<UserResponse>> getAllStaff(
      @RequestParam(required = false, defaultValue = "all") String status) {
    return ResponseEntity.ok(
        userService.getUsersByActiveStatus(status).stream()
            .map(userMapper::toResponse)
            .collect(Collectors.toList()));
  }

  @GetMapping("/{id}/any")
  public ResponseEntity<UserResponse> getAnyStaffById(@PathVariable long id) {
    User user = userService.findUserById(id);
    if (user == null) {
      return ResponseEntity.notFound().build();
    }
    return ResponseEntity.ok(userMapper.toResponse(user));
  }

  @GetMapping("/role/{role}")
  public ResponseEntity<List<UserResponse>> getStaffByRole(@PathVariable String role) {
    return ResponseEntity.ok(
        userService.findUserByRole(role).stream()
            .map(userMapper::toResponse)
            .collect(Collectors.toList()));
  }

  @PatchMapping("/{id}")
  public ResponseEntity<UserResponse> updateUser(
      @PathVariable long id, @Valid @RequestBody UserUpdateRequest request) {
    return ResponseEntity.ok(userMapper.toResponse(userService.updateUser(id, request)));
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<String> deleteStaff(@PathVariable long id) {
    userService.hardDeleteUser(id);
    return ResponseEntity.ok("User permanently deleted.");
  }

  @PostMapping("/{id}/photo")
  public ResponseEntity<String> uploadUserPhoto(
      @PathVariable long id, @RequestParam("file") MultipartFile file) {
    String imageUrl = userService.uploadUserPhoto(id, file);
    return ResponseEntity.ok(imageUrl);
  }

  @PostMapping("/{id}/resume")
  public ResponseEntity<String> uploadUserResume(
      @PathVariable long id, @RequestParam("file") MultipartFile file) {
    String fileUrl = userService.uploadUserResume(id, file);
    return ResponseEntity.ok(fileUrl);
  }

  @PostMapping("/{id}/barangay")
  public ResponseEntity<String> uploadUserBarangay(
      @PathVariable long id, @RequestParam("file") MultipartFile file) {
    String fileUrl = userService.uploadUserBarangay(id, file);
    return ResponseEntity.ok(fileUrl);
  }

  @PostMapping("/{id}/change-password")
  public ResponseEntity<?> changePassword(
          @PathVariable Long id,
          @Valid @RequestBody ChangePasswordRequest request,
          Authentication authentication) {

    userService.changePasswordWithAuthorization(id, request, authentication.getName());

    return ResponseEntity.ok(Map.of("message", "Password changed successfully. Please log in again."));
  }
}
