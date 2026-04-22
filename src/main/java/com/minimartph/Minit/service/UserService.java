package com.minimartph.Minit.service;

import com.minimartph.Minit.dto.UserRegistrationRequest;
import com.minimartph.Minit.dto.UserUpdateRequest;
import com.minimartph.Minit.entity.User;
import com.minimartph.Minit.enums.Role;
import com.minimartph.Minit.exceptions.*;
import com.minimartph.Minit.repository.UserRepository;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UserService implements UserDetailsService {

  private final UserRepository userRepository;
  private final BCryptPasswordEncoder bCryptPasswordEncoder;
  private final SaleService saleService;
  private final EmailService emailService;
  private final ImageStorageService imageStorageService;
  private final FileStorageService fileStorageService;

  public UserService(
      UserRepository userRepository,
      BCryptPasswordEncoder bCryptPasswordEncoder,
      SaleService saleService,
      EmailService emailService,
      ImageStorageService imageStorageService,
      FileStorageService fileStorageService) {
    this.userRepository = userRepository;
    this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    this.saleService = saleService;
    this.emailService = emailService;
    this.imageStorageService = imageStorageService;
    this.fileStorageService = fileStorageService;
  }

  // ---------- Registration ----------
  public User registerUser(UserRegistrationRequest request) {
    if (userRepository.existsByUsername(request.getUsername())) {
      throw new DuplicateResourceException("User", "username", request.getUsername());
    }

    if (userRepository.existsByEmail(request.getEmail())) {
      throw new DuplicateResourceException("User", "email", request.getEmail());
    }

    String rawPassword = request.getPassword(); // capture raw password
    String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
    User user = new User();
    user.setFirstName(request.getFirstName());
    user.setLastName(request.getLastName());
    user.setFullName(
        request.getFullName() != null
            ? request.getFullName()
            : request.getFirstName() + " " + request.getLastName());
    user.setGender(request.getGender());
    user.setEmail(request.getEmail());
    user.setPhoneNumber(request.getPhoneNumber());
    user.setAddress(request.getAddress());
    user.setAge(request.getAge());
    user.setUsername(request.getUsername());
    user.setPassword(encodedPassword);
    user.setRole(Role.valueOf(request.getRole()));
    user.setActive(true);
    user.setPhotoUrl(request.getPhotoUrl());
    user.setResumeUrl(request.getResumeUrl());
    user.setBarangayClearanceUrl(request.getBarangayClearanceUrl());
    User savedUser = userRepository.save(user);

    // Send email (handle exception to avoid breaking registration)
    try {
      emailService.sendCredentials(
          request.getEmail(), request.getUsername(), rawPassword, user.getFirstName());
    } catch (Exception e) {
      // Log error but don't prevent registration
      System.err.println("Failed to send email: " + e.getMessage());
    }

    return savedUser;
  }

  // Update user details
  @Transactional
  public User updateUser(Long id, UserUpdateRequest request) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

    // Text fields
    if (request.getFirstName() != null) user.setFirstName(request.getFirstName());
    if (request.getLastName() != null) user.setLastName(request.getLastName());
    if (request.getEmail() != null) user.setEmail(request.getEmail());
    if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
    if (request.getAddress() != null) user.setAddress(request.getAddress());
    if (request.getAge() != null) user.setAge(request.getAge());
    if (request.getRole() != null) user.setRole(Role.valueOf(request.getRole()));
    if (request.getActive() != null) user.setActive(request.getActive());
    if (request.getGender() != null) user.setGender(request.getGender());

    // Photo – if non‑null, store as is (base64)
    if (request.getPhotoUrl() != null) {
      user.setPhotoUrl(request.getPhotoUrl().isEmpty() ? null : request.getPhotoUrl());
    }

    // Resume – treat empty string as deletion, null as no change
    if (request.getResumeUrl() != null) {
      if (request.getResumeUrl().isEmpty()) {
        // Delete the file and set to null
        if (user.getResumeUrl() != null) {
          fileStorageService.deleteFile(user.getResumeUrl());
        }
        user.setResumeUrl(null);
      } else {
        // Update to new URL, delete old only if different
        if (user.getResumeUrl() != null && !user.getResumeUrl().equals(request.getResumeUrl())) {
          fileStorageService.deleteFile(user.getResumeUrl());
        }
        user.setResumeUrl(request.getResumeUrl());
      }
    }
    // If request.getResumeUrl() is null, do nothing (keep existing)

    // Same for Barangay Clearance
    if (request.getBarangayClearanceUrl() != null) {
      if (request.getBarangayClearanceUrl().isEmpty()) {
        if (user.getBarangayClearanceUrl() != null) {
          fileStorageService.deleteFile(user.getBarangayClearanceUrl());
        }
        user.setBarangayClearanceUrl(null);
      } else {
        if (user.getBarangayClearanceUrl() != null
            && !user.getBarangayClearanceUrl().equals(request.getBarangayClearanceUrl())) {
          fileStorageService.deleteFile(user.getBarangayClearanceUrl());
        }
        user.setBarangayClearanceUrl(request.getBarangayClearanceUrl());
      }
    }

    user.setFullName(user.getFirstName() + " " + user.getLastName());
    return userRepository.save(user);
  }

  // ---------- Spring Security UserDetailsService ----------
  @Override
  public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
    User user =
        userRepository
            .findByUsernameAndActiveTrue(username)
            .orElseThrow(() -> new ResourceNotFoundException("User", username));

    return new org.springframework.security.core.userdetails.User(
        user.getUsername(), user.getPassword(), getAuthorities(user.getRole()));
  }

  private Collection<? extends GrantedAuthority> getAuthorities(Role role) {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }

  // ---------- Query methods  ----------

  public User findActiveUserByUsername(String username) {
    return userRepository.findByUsernameAndActiveTrue(username).orElse(null);
  }

  public long countActiveAdmins() {
    return userRepository.countByRoleAndActiveTrue(Role.ADMIN);
  }

  public List<User> getUsersByActiveStatus(String status) {
    if ("active".equalsIgnoreCase(status)) {
      return userRepository.findAllByActiveTrue();
    } else if ("inactive".equalsIgnoreCase(status)) {
      return userRepository.findAll().stream()
          .filter(u -> !u.isActive())
          .collect(Collectors.toList());
    } else { // "all" or any other value
      return userRepository.findAll();
    }
  }

  public User findUserById(long id) {
    return userRepository.findById(id).orElse(null);
  }

  public User findUserByUsername(String username) {
    return userRepository.findByUsername(username).orElse(null);
  }

  public List<User> findUserByRole(String role) {
    return userRepository.findByRole(Role.valueOf(role));
  }

  // Update photo, resume, and barangay
  @Transactional
  public void updateUserResume(Long id, String newResumeUrl) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    if (user.getResumeUrl() != null) {
      fileStorageService.deleteFile(user.getResumeUrl());
    }
    user.setResumeUrl(newResumeUrl);
    userRepository.save(user);
  }

  @Transactional
  public void updateUserPhoto(Long id, String newPhotoUrl) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    // Delete an old file if exists
    if (user.getPhotoUrl() != null) {
      fileStorageService.deleteFile(user.getPhotoUrl());
    }
    user.setPhotoUrl(newPhotoUrl);
    userRepository.save(user);
  }

  @Transactional
  public void updateUserBarangay(Long id, String newBarangayUrl) {
    User user =
        userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
    if (user.getBarangayClearanceUrl() != null) {
      fileStorageService.deleteFile(user.getBarangayClearanceUrl());
    }
    user.setBarangayClearanceUrl(newBarangayUrl);
    userRepository.save(user);
  }

  // Delete user
  @Transactional
  public void hardDeleteUser(Long idToDelete) {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !auth.isAuthenticated()) {
      throw new InvalidCredentialsException("Not authenticated");
    }

    User currentUser = findActiveUserByUsername(auth.getName());
    if (currentUser == null) {
      throw new ResourceNotFoundException("Current user");
    }

    User userToDelete =
        userRepository
            .findById(idToDelete)
            .orElseThrow(() -> new ResourceNotFoundException("User", idToDelete));

    if (currentUser.getId().equals(idToDelete)) {
      throw new AppException(
          ErrorCode.CANNOT_DELETE_OWN_ACCOUNT, "You cannot delete your own account");
    }

    if (userToDelete.getRole() == Role.ADMIN && countActiveAdmins() <= 1) {
      throw new AppException(
          ErrorCode.CANNOT_DELETE_LAST_ADMIN, "Cannot delete the only remaining admin");
    }

    if (saleService.hasSales(idToDelete)) {
      throw new AppException(
          ErrorCode.USER_HAS_SALES_RECORDS,
          "Cannot delete user with existing sales. Deactivate them instead.");
    }

    userRepository.deleteById(idToDelete);
  }

  // Upload methods
  public String uploadUserPhoto(Long id, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    String imageUrl = imageStorageService.storeImage(file);
    updateUserPhoto(id, imageUrl);
    return imageUrl;
  }

  public String uploadUserResume(Long id, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    String fileName = fileStorageService.storeFile(file);
    String fileUrl = "/uploads/" + fileName;
    updateUserResume(id, fileUrl);
    return fileUrl;
  }

  public String uploadUserBarangay(Long id, MultipartFile file) {
    if (file == null || file.isEmpty()) {
      throw new IllegalArgumentException("File is empty");
    }
    String fileName = fileStorageService.storeFile(file);
    String fileUrl = "/uploads/" + fileName;
    updateUserBarangay(id, fileUrl);
    return fileUrl;
  }

  @Transactional
  public void changePassword(
      Long id, String currentPassword, String newPassword, String confirmPassword) {

    // Validate inputs
    if (newPassword == null || confirmPassword == null) {
      throw new AppException(
          ErrorCode.VALIDATION_ERROR, "New password and confirmation are required");
    }

    // Validate password length
    if (newPassword.length() < 6) {
      throw new AppException(
          ErrorCode.VALIDATION_ERROR, "New password must be at least 6 characters");
    }

    // Validate that new password and confirm the password match
    if (!newPassword.equals(confirmPassword)) {
      throw new AppException(
          ErrorCode.VALIDATION_ERROR, "New password and confirmation do not match");
    }

    // Find user by id
    User user =
        userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("User", id));

    // Verify current password matches
    if (!bCryptPasswordEncoder.matches(currentPassword, user.getPassword())) {
      throw new InvalidCredentialsException("Current password is incorrect");
    }

    // Encode and set new password
    String encodedNewPassword = bCryptPasswordEncoder.encode(newPassword);
    user.setPassword(encodedNewPassword);

    userRepository.save(user);
  }
}
