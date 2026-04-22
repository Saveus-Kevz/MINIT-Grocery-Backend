package com.minimartph.Minit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import com.minimartph.Minit.dto.UserRegistrationRequest;
import com.minimartph.Minit.dto.UserUpdateRequest;
import com.minimartph.Minit.entity.User;
import com.minimartph.Minit.enums.Gender;
import com.minimartph.Minit.enums.Role;
import com.minimartph.Minit.exceptions.AppException;
import com.minimartph.Minit.exceptions.DuplicateResourceException;
import com.minimartph.Minit.exceptions.InvalidCredentialsException;
import com.minimartph.Minit.exceptions.ResourceNotFoundException;
import com.minimartph.Minit.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Collections;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

  @Mock private UserRepository userRepository;

  @Mock private BCryptPasswordEncoder passwordEncoder;

  @Mock private SaleService saleService;

  @Mock private EmailService emailService;

  @Mock private ImageStorageService imageStorageService;

  @Mock private FileStorageService fileStorageService;

  @Mock private Authentication authentication;

  @Mock private SecurityContext securityContext;

  @Mock private MultipartFile mockFile;

  @InjectMocks private UserService userService;

  private User sampleAdmin;
  private User sampleCashier;
  private User sampleCustodial;
  private UserRegistrationRequest sampleRegistrationRequest;
  private UserUpdateRequest sampleUpdateRequest;

  @BeforeEach
  void setUp() {
    SecurityContextHolder.setContext(securityContext);

    // Sample Admin User
    sampleAdmin = new User();
    sampleAdmin.setId(1L);
    sampleAdmin.setFirstName("Admin");
    sampleAdmin.setLastName("User");
    sampleAdmin.setFullName("Admin User");
    sampleAdmin.setEmail("admin@example.com");
    sampleAdmin.setGender(Gender.MALE);
    sampleAdmin.setPhoneNumber("09123456789");
    sampleAdmin.setAddress("Admin Address");
    sampleAdmin.setAge("30");
    sampleAdmin.setUsername("admin");
    sampleAdmin.setPassword("encoded_admin_password");
    sampleAdmin.setRole(Role.ADMIN);
    sampleAdmin.setActive(true);
    sampleAdmin.setCreatedDateTime(LocalDateTime.now());

    // Sample Cashier User
    sampleCashier = new User();
    sampleCashier.setId(2L);
    sampleCashier.setFirstName("Cashier");
    sampleCashier.setLastName("User");
    sampleCashier.setFullName("Cashier User");
    sampleCashier.setEmail("cashier@example.com");
    sampleCashier.setGender(Gender.FEMALE);
    sampleCashier.setPhoneNumber("09876543210");
    sampleCashier.setAddress("Cashier Address");
    sampleCashier.setAge("25");
    sampleCashier.setUsername("cashier");
    sampleCashier.setPassword("encoded_cashier_password");
    sampleCashier.setRole(Role.CASHIER);
    sampleCashier.setActive(true);
    sampleCashier.setCreatedDateTime(LocalDateTime.now());

    // Sample Custodial User
    sampleCustodial = new User();
    sampleCustodial.setId(3L);
    sampleCustodial.setFirstName("Custodial");
    sampleCustodial.setLastName("User");
    sampleCustodial.setFullName("Custodial User");
    sampleCustodial.setEmail("custodial@example.com");
    sampleCustodial.setGender(Gender.OTHER);
    sampleCustodial.setPhoneNumber("09123456788");
    sampleCustodial.setAddress("Custodial Address");
    sampleCustodial.setAge("28");
    sampleCustodial.setUsername("custodial");
    sampleCustodial.setPassword("encoded_custodial_password");
    sampleCustodial.setRole(Role.CUSTODIAL);
    sampleCustodial.setActive(true);
    sampleCustodial.setCreatedDateTime(LocalDateTime.now());

    // Sample Registration Request
    sampleRegistrationRequest = new UserRegistrationRequest();
    sampleRegistrationRequest.setFirstName("New");
    sampleRegistrationRequest.setLastName("User");
    sampleRegistrationRequest.setEmail("newuser@example.com");
    sampleRegistrationRequest.setGender(Gender.MALE);
    sampleRegistrationRequest.setPhoneNumber("09123456777");
    sampleRegistrationRequest.setAddress("New Address");
    sampleRegistrationRequest.setAge("22");
    sampleRegistrationRequest.setUsername("newuser");
    sampleRegistrationRequest.setPassword("password123");
    sampleRegistrationRequest.setRole("CASHIER");

    // Sample Update Request
    sampleUpdateRequest = new UserUpdateRequest();
    sampleUpdateRequest.setFirstName("Updated");
    sampleUpdateRequest.setLastName("Name");
    sampleUpdateRequest.setEmail("updated@example.com");
    sampleUpdateRequest.setPhoneNumber("09999999999");
    sampleUpdateRequest.setAddress("Updated Address");
    sampleUpdateRequest.setAge("35");
    sampleUpdateRequest.setGender(Gender.FEMALE);
    sampleUpdateRequest.setRole("ADMIN");
    sampleUpdateRequest.setActive(true);
  }

  // ========== REGISTRATION TESTS ==========

  @Nested
  @DisplayName("registerUser()")
  class RegisterUserTests {

    @Test
    @DisplayName("Should register user successfully when valid data provided")
    void shouldRegisterUserSuccessfully() {
      // Given
      String rawPassword = "password123";
      String encodedPassword = "encoded_password123";

      when(userRepository.existsByUsername("newuser")).thenReturn(false);
      when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
      when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
      when(userRepository.save(any(User.class)))
          .thenAnswer(
              invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(4L);
                return savedUser;
              });
      doNothing()
          .when(emailService)
          .sendCredentials(anyString(), anyString(), anyString(), anyString());

      // When
      User result = userService.registerUser(sampleRegistrationRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(4L);
      assertThat(result.getUsername()).isEqualTo("newuser");
      assertThat(result.getEmail()).isEqualTo("newuser@example.com");
      assertThat(result.getRole()).isEqualTo(Role.CASHIER);
      assertThat(result.isActive()).isTrue();

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());

      User savedUser = userCaptor.getValue();
      assertThat(savedUser.getPassword()).isEqualTo(encodedPassword);
      assertThat(savedUser.getFullName()).isEqualTo("New User");

      verify(emailService).sendCredentials("newuser@example.com", "newuser", rawPassword, "New");
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when username already exists")
    void shouldThrowExceptionWhenUsernameExists() {
      // Given
      when(userRepository.existsByUsername("newuser")).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> userService.registerUser(sampleRegistrationRequest))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("User with username 'newuser' already exists");

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should throw DuplicateResourceException when email already exists")
    void shouldThrowExceptionWhenEmailExists() {
      // Given
      when(userRepository.existsByUsername("newuser")).thenReturn(false);
      when(userRepository.existsByEmail("newuser@example.com")).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> userService.registerUser(sampleRegistrationRequest))
          .isInstanceOf(DuplicateResourceException.class)
          .hasMessageContaining("User with email 'newuser@example.com' already exists");

      verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Should handle email sending failure gracefully")
    void shouldHandleEmailFailureGracefully() {
      // Given
      when(userRepository.existsByUsername("newuser")).thenReturn(false);
      when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
      when(passwordEncoder.encode(anyString())).thenReturn("encoded");
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);
      doThrow(new RuntimeException("Email service down"))
          .when(emailService)
          .sendCredentials(anyString(), anyString(), anyString(), anyString());

      // When
      User result = userService.registerUser(sampleRegistrationRequest);

      // Then
      assertThat(result).isNotNull();
      verify(userRepository).save(any(User.class));
      // Verify email was attempted even though it failed
      verify(emailService)
          .sendCredentials(
              "newuser@example.com", "newuser", sampleRegistrationRequest.getPassword(), "New");
    }

    @Test
    @DisplayName("Should create user with ADMIN role")
    void shouldCreateUserWithAdminRole() {
      // Given
      UserRegistrationRequest adminRequest = new UserRegistrationRequest();
      adminRequest.setFirstName("New");
      adminRequest.setLastName("Admin");
      adminRequest.setEmail("newadmin@example.com");
      adminRequest.setUsername("newadmin");
      adminRequest.setPassword("password123");
      adminRequest.setRole("ADMIN");
      adminRequest.setGender(Gender.MALE);
      adminRequest.setPhoneNumber("09123456777");
      adminRequest.setAddress("Admin Address");
      adminRequest.setAge("30");

      when(userRepository.existsByUsername("newadmin")).thenReturn(false);
      when(userRepository.existsByEmail("newadmin@example.com")).thenReturn(false);
      when(passwordEncoder.encode("password123")).thenReturn("encoded_admin_password");
      when(userRepository.save(any(User.class)))
          .thenAnswer(
              invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(5L);
                return savedUser;
              });
      doNothing()
          .when(emailService)
          .sendCredentials(anyString(), anyString(), anyString(), anyString());

      // When
      User result = userService.registerUser(adminRequest);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getRole()).isEqualTo(Role.ADMIN);

      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Should set full name from first and last name if not provided")
    void shouldSetFullNameFromFirstAndLastName() {
      // Given
      when(userRepository.existsByUsername("newuser")).thenReturn(false);
      when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
      when(passwordEncoder.encode(anyString())).thenReturn("encoded_password");
      when(userRepository.save(any(User.class)))
          .thenAnswer(
              invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(6L);
                return savedUser;
              });
      doNothing()
          .when(emailService)
          .sendCredentials(anyString(), anyString(), anyString(), anyString());

      // When
      User result = userService.registerUser(sampleRegistrationRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getFullName()).isEqualTo("New User");
    }

    @Test
    @DisplayName("Should use provided full name when not null")
    void shouldUseProvidedFullNameWhenNotNull() {
      // Given - fullName is explicitly provided
      UserRegistrationRequest requestWithFullName = new UserRegistrationRequest();
      requestWithFullName.setFirstName("John");
      requestWithFullName.setLastName("Doe");
      requestWithFullName.setFullName("John Q. Doe"); // Explicit full name
      requestWithFullName.setEmail("john@example.com");
      requestWithFullName.setUsername("johndoe");
      requestWithFullName.setPassword("password123");
      requestWithFullName.setRole("CASHIER");
      requestWithFullName.setGender(Gender.MALE);
      requestWithFullName.setPhoneNumber("09123456789");
      requestWithFullName.setAddress("123 Main St");
      requestWithFullName.setAge("30");

      when(userRepository.existsByUsername("johndoe")).thenReturn(false);
      when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
      when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
      when(userRepository.save(any(User.class)))
          .thenAnswer(
              invocation -> {
                User savedUser = invocation.getArgument(0);
                savedUser.setId(7L);
                return savedUser;
              });
      doNothing()
          .when(emailService)
          .sendCredentials(anyString(), anyString(), anyString(), anyString());

      // When
      User result = userService.registerUser(requestWithFullName);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getFullName()).isEqualTo("John Q. Doe");
    }
  }

  // ========== USER DETAILS SERVICE TESTS ==========

  @Nested
  @DisplayName("loadUserByUsername()")
  class LoadUserByUsernameTests {

    @Test
    @DisplayName("Should load user details successfully when user exists and is active")
    void shouldLoadUserDetailsSuccessfully() {
      // Given
      when(userRepository.findByUsernameAndActiveTrue("admin"))
          .thenReturn(Optional.of(sampleAdmin));

      // When
      UserDetails result = userService.loadUserByUsername("admin");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("admin");
      assertThat(result.getPassword()).isEqualTo("encoded_admin_password");
      assertThat(result.getAuthorities()).hasSize(1);
      assertThat(result.getAuthorities().iterator().next().getAuthority()).isEqualTo("ROLE_ADMIN");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findByUsernameAndActiveTrue("unknown")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.loadUserByUsername("unknown"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User not found with id: unknown");
    }
  }

  // ========== QUERY METHODS TESTS ==========

  @Nested
  @DisplayName("findActiveUserByUsername()")
  class FindActiveUserByUsernameTests {

    @Test
    @DisplayName("Should return user when found and active")
    void shouldReturnUserWhenFoundAndActive() {
      // Given
      when(userRepository.findByUsernameAndActiveTrue("admin"))
          .thenReturn(Optional.of(sampleAdmin));

      // When
      User result = userService.findActiveUserByUsername("admin");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Should return null when user not found")
    void shouldReturnNullWhenUserNotFound() {
      // Given
      when(userRepository.findByUsernameAndActiveTrue("unknown")).thenReturn(Optional.empty());

      // When
      User result = userService.findActiveUserByUsername("unknown");

      // Then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("countActiveAdmins()")
  class CountActiveAdminsTests {

    @Test
    @DisplayName("Should return count of active admins")
    void shouldReturnCountOfActiveAdmins() {
      // Given
      when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(2L);

      // When
      long result = userService.countActiveAdmins();

      // Then
      assertThat(result).isEqualTo(2L);
      verify(userRepository).countByRoleAndActiveTrue(Role.ADMIN);
    }
  }

  @Nested
  @DisplayName("getUsersByActiveStatus()")
  class GetUsersByActiveStatusTests {

    @Test
    @DisplayName("Should return active users when status is 'active'")
    void shouldReturnActiveUsers() {
      // Given
      List<User> activeUsers = List.of(sampleAdmin, sampleCashier);
      when(userRepository.findAllByActiveTrue()).thenReturn(activeUsers);

      // When
      List<User> result = userService.getUsersByActiveStatus("active");

      // Then
      assertThat(result).hasSize(2);
      verify(userRepository).findAllByActiveTrue();
    }

    @Test
    @DisplayName("Should return inactive users when status is 'inactive'")
    void shouldReturnInactiveUsers() {
      // Given
      sampleAdmin.setActive(false);
      List<User> allUsers = List.of(sampleAdmin, sampleCashier);
      when(userRepository.findAll()).thenReturn(allUsers);

      // When
      List<User> result = userService.getUsersByActiveStatus("inactive");

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).isActive()).isFalse();
    }

    @Test
    @DisplayName("Should return all users when status is 'all'")
    void shouldReturnAllUsers() {
      // Given
      List<User> allUsers = List.of(sampleAdmin, sampleCashier);
      when(userRepository.findAll()).thenReturn(allUsers);

      // When
      List<User> result = userService.getUsersByActiveStatus("all");

      // Then
      assertThat(result).hasSize(2);
      verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no active users exist")
    void shouldReturnEmptyListWhenNoActiveUsersExist() {
      // Given
      when(userRepository.findAllByActiveTrue()).thenReturn(Collections.emptyList());

      // When
      List<User> result = userService.getUsersByActiveStatus("active");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty list when no inactive users exist")
    void shouldReturnEmptyListWhenNoInactiveUsersExist() {
      // Given
      List<User> allUsers = List.of(sampleAdmin, sampleCashier);
      when(userRepository.findAll()).thenReturn(allUsers);

      // When
      List<User> result = userService.getUsersByActiveStatus("inactive");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return all users when status is unknown value")
    void shouldReturnAllUsersWhenStatusIsUnknown() {
      // Given
      List<User> allUsers = List.of(sampleAdmin, sampleCashier);
      when(userRepository.findAll()).thenReturn(allUsers);

      // When
      List<User> result = userService.getUsersByActiveStatus("unknown");

      // Then
      assertThat(result).hasSize(2);
      verify(userRepository).findAll();
    }

    @Test
    @DisplayName("Should be case insensitive for status 'ACTIVE'")
    void shouldBeCaseInsensitiveForActive() {
      // Given
      List<User> activeUsers = List.of(sampleAdmin, sampleCashier);
      when(userRepository.findAllByActiveTrue()).thenReturn(activeUsers);

      // When
      List<User> result = userService.getUsersByActiveStatus("ACTIVE");

      // Then
      assertThat(result).hasSize(2);
      verify(userRepository).findAllByActiveTrue();
    }

    @Test
    @DisplayName("Should be case insensitive for status 'INACTIVE'")
    void shouldBeCaseInsensitiveForInactive() {
      // Given
      sampleAdmin.setActive(false);
      List<User> allUsers = List.of(sampleAdmin, sampleCashier);
      when(userRepository.findAll()).thenReturn(allUsers);

      // When
      List<User> result = userService.getUsersByActiveStatus("INACTIVE");

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).isActive()).isFalse();
    }
  }

  @Nested
  @DisplayName("findUserById()")
  class FindUserByIdTests {

    @Test
    @DisplayName("Should return user when found")
    void shouldReturnUserWhenFound() {
      // Given
      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));

      // When
      User result = userService.findUserById(1L);

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should return null when user not found")
    void shouldReturnNullWhenNotFound() {
      // Given
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // When
      User result = userService.findUserById(999L);

      // Then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("findUserByUsername()")
  class FindUserByUsernameTests {

    @Test
    @DisplayName("Should return user when found")
    void shouldReturnUserWhenFound() {
      // Given
      when(userRepository.findByUsername("admin")).thenReturn(Optional.of(sampleAdmin));

      // When
      User result = userService.findUserByUsername("admin");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getUsername()).isEqualTo("admin");
    }

    @Test
    @DisplayName("Should return null when user not found")
    void shouldReturnNullWhenNotFound() {
      // Given
      when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

      // When
      User result = userService.findUserByUsername("unknown");

      // Then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("findUserByRole()")
  class FindUserByRoleTests {

    @Test
    @DisplayName("Should return users by role")
    void shouldReturnUsersByRole() {
      // Given
      List<User> cashiers = List.of(sampleCashier);
      when(userRepository.findByRole(Role.CASHIER)).thenReturn(cashiers);

      // When
      List<User> result = userService.findUserByRole("CASHIER");

      // Then
      assertThat(result).hasSize(1);
      assertThat(result.get(0).getRole()).isEqualTo(Role.CASHIER);
    }

    @Test
    @DisplayName("Should return empty list when no users with role exist")
    void shouldReturnEmptyListWhenNoUsersWithRoleExist() {
      // Given
      when(userRepository.findByRole(Role.ADMIN)).thenReturn(Collections.emptyList());

      // When
      List<User> result = userService.findUserByRole("ADMIN");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should handle multiple users with same role")
    void shouldHandleMultipleUsersWithSameRole() {
      // Given
      User anotherCashier = new User();
      anotherCashier.setId(4L);
      anotherCashier.setUsername("cashier2");
      anotherCashier.setRole(Role.CASHIER);
      List<User> cashiers = List.of(sampleCashier, anotherCashier);
      when(userRepository.findByRole(Role.CASHIER)).thenReturn(cashiers);

      // When
      List<User> result = userService.findUserByRole("CASHIER");

      // Then
      assertThat(result).hasSize(2);
      assertThat(result).allMatch(u -> u.getRole() == Role.CASHIER);
    }
  }

  // ========== UPDATE METHODS TESTS ==========

  @Nested
  @DisplayName("updateUser()")
  class UpdateUserTests {

    @Test
    @DisplayName("Should update user fields successfully")
    void shouldUpdateUserFieldsSuccessfully() {
      // Given
      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, sampleUpdateRequest);

      // Then
      assertThat(result).isNotNull();
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());

      User updatedUser = userCaptor.getValue();
      assertThat(updatedUser.getFirstName()).isEqualTo("Updated");
      assertThat(updatedUser.getLastName()).isEqualTo("Name");
      assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
      assertThat(updatedUser.getRole()).isEqualTo(Role.ADMIN);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.updateUser(999L, sampleUpdateRequest))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("User not found");
    }

    @Test
    @DisplayName("Should skip updates when fields are null")
    void shouldSkipUpdatesWhenFieldsAreNull() {
      // Given
      UserUpdateRequest nullRequest = new UserUpdateRequest();
      nullRequest.setFirstName(null);
      nullRequest.setLastName(null);
      nullRequest.setEmail(null);
      // All fields are null

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, nullRequest);

      // Then
      assertThat(result).isNotNull();
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      // Values should remain unchanged
      assertThat(userCaptor.getValue().getFirstName()).isEqualTo("Cashier");
    }

    @Test
    @DisplayName("Should update photo URL when non-empty string provided")
    void shouldUpdatePhotoUrlWhenNonEmpty() {
      // Given
      UserUpdateRequest photoRequest = new UserUpdateRequest();
      photoRequest.setPhotoUrl("new_photo_base64");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, photoRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getPhotoUrl()).isEqualTo("new_photo_base64");
    }

    @Test
    @DisplayName("Should set photo URL to null when empty string provided")
    void shouldSetPhotoUrlToNullWhenEmpty() {
      // Given
      UserUpdateRequest photoRequest = new UserUpdateRequest();
      photoRequest.setPhotoUrl("");
      sampleCashier.setPhotoUrl("old_photo.jpg");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, photoRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getPhotoUrl()).isNull();
    }

    @Test
    @DisplayName("Should handle resume URL update when new URL provided")
    void shouldUpdateResumeUrlWhenNewUrlProvided() {
      // Given
      UserUpdateRequest resumeRequest = new UserUpdateRequest();
      resumeRequest.setResumeUrl("/uploads/new_resume.pdf");
      sampleCashier.setResumeUrl("/uploads/old_resume.pdf");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, resumeRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      // Old file should be deleted
      verify(fileStorageService).deleteFile("/uploads/old_resume.pdf");
      assertThat(userCaptor.getValue().getResumeUrl()).isEqualTo("/uploads/new_resume.pdf");
    }

    @Test
    @DisplayName("Should delete resume and set to null when empty string provided")
    void shouldDeleteResumeWhenEmptyStringProvided() {
      // Given
      UserUpdateRequest resumeRequest = new UserUpdateRequest();
      resumeRequest.setResumeUrl("");
      sampleCashier.setResumeUrl("/uploads/resume.pdf");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, resumeRequest);

      // Then
      verify(fileStorageService).deleteFile("/uploads/resume.pdf");
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getResumeUrl()).isNull();
    }

    @Test
    @DisplayName("Should not delete resume if same URL provided")
    void shouldNotDeleteResumeIfSameUrlProvided() {
      // Given
      UserUpdateRequest resumeRequest = new UserUpdateRequest();
      resumeRequest.setResumeUrl("/uploads/resume.pdf");
      sampleCashier.setResumeUrl("/uploads/resume.pdf");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, resumeRequest);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should set new resume when old resume is null")
    void shouldSetResumeWhenOldResumeIsNull() {
      // Given
      UserUpdateRequest resumeRequest = new UserUpdateRequest();
      resumeRequest.setResumeUrl("/uploads/new_resume.pdf");
      sampleCashier.setResumeUrl(null);

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, resumeRequest);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getResumeUrl()).isEqualTo("/uploads/new_resume.pdf");
    }

    @Test
    @DisplayName("Should keep existing resume when request resumeUrl is null")
    void shouldKeepExistingResumeWhenRequestResumeUrlIsNull() {
      // Given
      UserUpdateRequest request = new UserUpdateRequest();
      request.setResumeUrl(null);
      sampleCashier.setResumeUrl("/uploads/old_resume.pdf");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, request);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getResumeUrl()).isEqualTo("/uploads/old_resume.pdf");
    }

    @Test
    @DisplayName("Should handle empty string when no resume exists")
    void shouldHandleEmptyStringWhenNoResumeExists() {
      // Given
      UserUpdateRequest resumeRequest = new UserUpdateRequest();
      resumeRequest.setResumeUrl(""); // Empty string triggers delete flow
      sampleCashier.setResumeUrl(null); // The current resume is already null

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, resumeRequest);

      // Then
      // Should NOT call deleteFile because user.getResumeUrl() is null
      verify(fileStorageService, never()).deleteFile(anyString());
      // Should still set resumeUrl to null (already null, but set it anyway)
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getResumeUrl()).isNull();
    }

    @Test
    @DisplayName("Should handle barangay clearance URL update")
    void shouldUpdateBarangayUrlWhenNewUrlProvided() {
      // Given
      UserUpdateRequest barangayRequest = new UserUpdateRequest();
      barangayRequest.setBarangayClearanceUrl("/uploads/new_barangay.pdf");
      sampleCashier.setBarangayClearanceUrl("/uploads/old_barangay.pdf");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, barangayRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      verify(fileStorageService).deleteFile("/uploads/old_barangay.pdf");
      assertThat(userCaptor.getValue().getBarangayClearanceUrl())
          .isEqualTo("/uploads/new_barangay.pdf");
    }

    @Test
    @DisplayName("Should handle barangay URL when old barangay is null")
    void shouldHandleBarangayUrlWhenOldBarangayIsNull() {
      // Given
      UserUpdateRequest barangayRequest = new UserUpdateRequest();
      barangayRequest.setBarangayClearanceUrl("/uploads/new_barangay.pdf");
      sampleCashier.setBarangayClearanceUrl(null);

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, barangayRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      // No delete should occur since old barangay was null
      verify(fileStorageService, never()).deleteFile(anyString());
      assertThat(userCaptor.getValue().getBarangayClearanceUrl())
          .isEqualTo("/uploads/new_barangay.pdf");
    }

    @Test
    @DisplayName("Should handle barangay URL deletion when empty string provided")
    void shouldDeleteBarangayWhenEmptyStringProvided() {
      // Given
      UserUpdateRequest barangayRequest = new UserUpdateRequest();
      barangayRequest.setBarangayClearanceUrl("");
      sampleCashier.setBarangayClearanceUrl("/uploads/barangay.pdf");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, barangayRequest);

      // Then
      verify(fileStorageService).deleteFile("/uploads/barangay.pdf");
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getBarangayClearanceUrl()).isNull();
    }

    @Test
    @DisplayName("Should not delete barangay if same URL provided")
    void shouldNotDeleteBarangayIfSameUrlProvided() {
      // Given
      UserUpdateRequest barangayRequest = new UserUpdateRequest();
      barangayRequest.setBarangayClearanceUrl("/uploads/barangay.pdf");
      sampleCashier.setBarangayClearanceUrl("/uploads/barangay.pdf");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, barangayRequest);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
    }

    @Test
    @DisplayName("Should handle barangay URL deletion when old barangay is null")
    void shouldHandleBarangayDeletionWhenOldIsNull() {
      // Given
      UserUpdateRequest barangayRequest = new UserUpdateRequest();
      barangayRequest.setBarangayClearanceUrl("");
      sampleCashier.setBarangayClearanceUrl(null);

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, barangayRequest);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getBarangayClearanceUrl()).isNull();
    }

    @Test
    @DisplayName("Should keep existing barangay when request barangayClearanceUrl is null")
    void shouldKeepExistingBarangayWhenRequestBarangayClearanceUrlIsNull() {
      // Given
      UserUpdateRequest request = new UserUpdateRequest();
      request.setBarangayClearanceUrl(null);
      sampleCashier.setBarangayClearanceUrl("/uploads/old_barangay.pdf");

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, request);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getBarangayClearanceUrl())
          .isEqualTo("/uploads/old_barangay.pdf");
    }

    @Test
    @DisplayName("Should set new barangay when old barangay is null")
    void shouldSetBarangayWhenOldBarangayIsNull() {
      // Given
      UserUpdateRequest barangayRequest = new UserUpdateRequest();
      barangayRequest.setBarangayClearanceUrl("/uploads/new_barangay.pdf");
      sampleCashier.setBarangayClearanceUrl(null);

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, barangayRequest);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getBarangayClearanceUrl())
          .isEqualTo("/uploads/new_barangay.pdf");
    }

    @Test
    @DisplayName("Should skip firstName update when firstName is null")
    void shouldSkipFirstNameUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setFirstName(null);
      partialRequest.setLastName("NewLast");
      String originalFirstName = sampleCashier.getFirstName();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getFirstName()).isEqualTo(originalFirstName);
      assertThat(userCaptor.getValue().getLastName()).isEqualTo("NewLast");
    }

    @Test
    @DisplayName("Should skip lastName update when lastName is null")
    void shouldSkipLastNameUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setFirstName("NewFirst");
      partialRequest.setLastName(null);
      String originalLastName = sampleCashier.getLastName();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getFirstName()).isEqualTo("NewFirst");
      assertThat(userCaptor.getValue().getLastName()).isEqualTo(originalLastName);
    }

    @Test
    @DisplayName("Should skip email update when email is null")
    void shouldSkipEmailUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setEmail(null);
      String originalEmail = sampleCashier.getEmail();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getEmail()).isEqualTo(originalEmail);
    }

    @Test
    @DisplayName("Should skip phoneNumber update when phoneNumber is null")
    void shouldSkipPhoneNumberUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setPhoneNumber(null);
      String originalPhoneNumber = sampleCashier.getPhoneNumber();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getPhoneNumber()).isEqualTo(originalPhoneNumber);
    }

    @Test
    @DisplayName("Should skip address update when address is null")
    void shouldSkipAddressUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setAddress(null);
      String originalAddress = sampleCashier.getAddress();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getAddress()).isEqualTo(originalAddress);
    }

    @Test
    @DisplayName("Should skip age update when age is null")
    void shouldSkipAgeUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setAge(null);
      String originalAge = sampleCashier.getAge();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getAge()).isEqualTo(originalAge);
    }

    @Test
    @DisplayName("Should skip role update when role is null")
    void shouldSkipRoleUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setRole(null);
      Role originalRole = sampleCashier.getRole();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getRole()).isEqualTo(originalRole);
    }

    @Test
    @DisplayName("Should skip active status update when active is null")
    void shouldSkipActiveStatusUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setActive(null);
      boolean originalActive = sampleCashier.isActive();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().isActive()).isEqualTo(originalActive);
    }

    @Test
    @DisplayName("Should skip gender update when gender is null")
    void shouldSkipGenderUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setGender(null);
      Gender originalGender = sampleCashier.getGender();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getGender()).isEqualTo(originalGender);
    }

    @Test
    @DisplayName("Should skip photoUrl update when photoUrl is null")
    void shouldSkipPhotoUrlUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setPhotoUrl(null);
      String originalPhotoUrl = sampleCashier.getPhotoUrl();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getPhotoUrl()).isEqualTo(originalPhotoUrl);
    }

    @Test
    @DisplayName("Should skip resumeUrl update when resumeUrl is null")
    void shouldSkipResumeUrlUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setResumeUrl(null);
      String originalResumeUrl = sampleCashier.getResumeUrl();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getResumeUrl()).isEqualTo(originalResumeUrl);
    }

    @Test
    @DisplayName("Should skip barangayClearanceUrl update when barangayClearanceUrl is null")
    void shouldSkipBarangayUrlUpdateWhenNull() {
      // Given
      UserUpdateRequest partialRequest = new UserUpdateRequest();
      partialRequest.setBarangayClearanceUrl(null);
      String originalBarangayUrl = sampleCashier.getBarangayClearanceUrl();

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      userService.updateUser(2L, partialRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getBarangayClearanceUrl()).isEqualTo(originalBarangayUrl);
    }

    @Test
    @DisplayName("Should handle resume URL when old resume is null")
    void shouldHandleResumeUrlWhenOldResumeIsNull() {
      // Given
      UserUpdateRequest resumeRequest = new UserUpdateRequest();
      resumeRequest.setResumeUrl("/uploads/new_resume.pdf");
      sampleCashier.setResumeUrl(null);

      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      when(userRepository.save(any(User.class))).thenReturn(sampleCashier);

      // When
      User result = userService.updateUser(2L, resumeRequest);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      // No delete should occur since old resume was null
      verify(fileStorageService, never()).deleteFile(anyString());
      assertThat(userCaptor.getValue().getResumeUrl()).isEqualTo("/uploads/new_resume.pdf");
    }
  }

  // ========== FILE UPLOAD TESTS ==========

  @Nested
  @DisplayName("uploadUserPhoto()")
  class UploadUserPhotoTests {

    @Test
    @DisplayName("Should upload photo successfully")
    void shouldUploadPhotoSuccessfully() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(imageStorageService.storeImage(mockFile)).thenReturn("/uploads/photo.jpg");
      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      String result = userService.uploadUserPhoto(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/photo.jpg");
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getPhotoUrl()).isEqualTo("/uploads/photo.jpg");
    }

    @Test
    @DisplayName("Should throw exception when file is empty")
    void shouldThrowExceptionWhenFileEmpty() {
      // Given
      when(mockFile.isEmpty()).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> userService.uploadUserPhoto(1L, mockFile))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("File is empty");
    }

    @Test
    @DisplayName("Should throw exception when file is null")
    void shouldThrowExceptionWhenFileNull() {
      // When & Then
      assertThatThrownBy(() -> userService.uploadUserPhoto(1L, null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("File is empty");
    }
  }

  @Nested
  @DisplayName("uploadUserResume()")
  class UploadUserResumeTests {

    @Test
    @DisplayName("Should upload resume successfully")
    void shouldUploadResumeSuccessfully() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(fileStorageService.storeFile(mockFile)).thenReturn("resume_123.pdf");
      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      String result = userService.uploadUserResume(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/resume_123.pdf");
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getResumeUrl()).isEqualTo("/uploads/resume_123.pdf");
    }

    @Test
    @DisplayName("Should throw exception when resume file is empty")
    void shouldThrowExceptionWhenResumeFileEmpty() {
      // Given
      when(mockFile.isEmpty()).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> userService.uploadUserResume(1L, mockFile))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("File is empty");
    }

    @Test
    @DisplayName("Should throw exception when resume file is null")
    void shouldThrowExceptionWhenResumeFileNull() {
      // When & Then
      assertThatThrownBy(() -> userService.uploadUserResume(1L, null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("File is empty");
    }
  }

  @Nested
  @DisplayName("uploadUserBarangay()")
  class UploadUserBarangayTests {

    @Test
    @DisplayName("Should upload barangay clearance successfully")
    void shouldUploadBarangaySuccessfully() {
      // Given
      when(mockFile.isEmpty()).thenReturn(false);
      when(fileStorageService.storeFile(mockFile)).thenReturn("barangay_123.pdf");
      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      String result = userService.uploadUserBarangay(1L, mockFile);

      // Then
      assertThat(result).isEqualTo("/uploads/barangay_123.pdf");
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getBarangayClearanceUrl())
          .isEqualTo("/uploads/barangay_123.pdf");
    }

    @Test
    @DisplayName("Should throw exception when barangay file is empty")
    void shouldThrowExceptionWhenBarangayFileEmpty() {
      // Given
      when(mockFile.isEmpty()).thenReturn(true);

      // When & Then
      assertThatThrownBy(() -> userService.uploadUserBarangay(1L, mockFile))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("File is empty");
    }

    @Test
    @DisplayName("Should throw exception when barangay file is null")
    void shouldThrowExceptionWhenBarangayFileNull() {
      // When & Then
      assertThatThrownBy(() -> userService.uploadUserBarangay(1L, null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessage("File is empty");
    }
  }

  @Nested
  @DisplayName("updateUserPhoto()")
  class UpdateUserPhotoTests {

    @Test
    @DisplayName("Should update user photo and delete old photo")
    void shouldUpdatePhotoAndDeleteOldPhoto() {
      // Given
      String newPhotoUrl = "/uploads/new_photo.jpg";
      sampleAdmin.setPhotoUrl("/uploads/old_photo.jpg");

      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      userService.updateUserPhoto(1L, newPhotoUrl);

      // Then
      verify(fileStorageService).deleteFile("/uploads/old_photo.jpg");
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getPhotoUrl()).isEqualTo(newPhotoUrl);
    }

    @Test
    @DisplayName("Should update photo when no old photo exists")
    void shouldUpdatePhotoWhenNoOldPhotoExists() {
      // Given
      String newPhotoUrl = "/uploads/new_photo.jpg";
      sampleAdmin.setPhotoUrl(null);

      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      userService.updateUserPhoto(1L, newPhotoUrl);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getPhotoUrl()).isEqualTo(newPhotoUrl);
    }

    @Test
    @DisplayName("Should throw exception when user not found during photo update")
    void shouldThrowExceptionWhenUserNotFoundDuringPhotoUpdate() {
      // Given
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.updateUserPhoto(999L, "/uploads/photo.jpg"))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("User not found");
    }
  }

  @Nested
  @DisplayName("updateUserResume()")
  class UpdateUserResumeTests {

    @Test
    @DisplayName("Should update user resume and delete old resume")
    void shouldUpdateResumeAndDeleteOldResume() {
      // Given
      String newResumeUrl = "/uploads/new_resume.pdf";
      sampleAdmin.setResumeUrl("/uploads/old_resume.pdf");

      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      userService.updateUserResume(1L, newResumeUrl);

      // Then
      verify(fileStorageService).deleteFile("/uploads/old_resume.pdf");
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getResumeUrl()).isEqualTo(newResumeUrl);
    }

    @Test
    @DisplayName("Should update resume when no old resume exists")
    void shouldUpdateResumeWhenNoOldResumeExists() {
      // Given
      String newResumeUrl = "/uploads/new_resume.pdf";
      sampleAdmin.setResumeUrl(null);

      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      userService.updateUserResume(1L, newResumeUrl);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getResumeUrl()).isEqualTo(newResumeUrl);
    }

    @Test
    @DisplayName("Should throw exception when user not found during resume update")
    void shouldThrowExceptionWhenUserNotFoundDuringResumeUpdate() {
      // Given
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.updateUserResume(999L, "/uploads/resume.pdf"))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("User not found");
    }
  }

  @Nested
  @DisplayName("updateUserBarangay()")
  class UpdateUserBarangayTests {

    @Test
    @DisplayName("Should update user barangay clearance and delete old file")
    void shouldUpdateBarangayAndDeleteOldFile() {
      // Given
      String newBarangayUrl = "/uploads/new_barangay.pdf";
      sampleAdmin.setBarangayClearanceUrl("/uploads/old_barangay.pdf");

      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      userService.updateUserBarangay(1L, newBarangayUrl);

      // Then
      verify(fileStorageService).deleteFile("/uploads/old_barangay.pdf");
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getBarangayClearanceUrl()).isEqualTo(newBarangayUrl);
    }

    @Test
    @DisplayName("Should update barangay when no old file exists")
    void shouldUpdateBarangayWhenNoOldFileExists() {
      // Given
      String newBarangayUrl = "/uploads/new_barangay.pdf";
      sampleAdmin.setBarangayClearanceUrl(null);

      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      userService.updateUserBarangay(1L, newBarangayUrl);

      // Then
      verify(fileStorageService, never()).deleteFile(anyString());
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getBarangayClearanceUrl()).isEqualTo(newBarangayUrl);
    }

    @Test
    @DisplayName("Should throw exception when user not found during barangay update")
    void shouldThrowExceptionWhenUserNotFoundDuringBarangayUpdate() {
      // Given
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.updateUserBarangay(999L, "/uploads/barangay.pdf"))
          .isInstanceOf(RuntimeException.class)
          .hasMessageContaining("User not found");
    }
  }

  // ========== PASSWORD CHANGE TESTS ==========

  @Nested
  @DisplayName("changePassword()")
  class ChangePasswordTests {

    @Test
    @DisplayName("Should change password successfully")
    void shouldChangePasswordSuccessfully() {
      // Given
      String currentPassword = "oldPassword";
      String newPassword = "newPassword123";
      String confirmPassword = "newPassword123";
      String encodedNewPassword = "encoded_new_password";

      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(passwordEncoder.matches(currentPassword, sampleAdmin.getPassword())).thenReturn(true);
      when(passwordEncoder.encode(newPassword)).thenReturn(encodedNewPassword);
      when(userRepository.save(any(User.class))).thenReturn(sampleAdmin);

      // When
      userService.changePassword(1L, currentPassword, newPassword, confirmPassword);

      // Then
      ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
      verify(userRepository).save(userCaptor.capture());
      assertThat(userCaptor.getValue().getPassword()).isEqualTo(encodedNewPassword);
    }

    @Test
    @DisplayName("Should throw exception when new password is null")
    void shouldThrowExceptionWhenNewPasswordIsNull() {
      assertThatThrownBy(() -> userService.changePassword(1L, "oldPassword", null, "newPassword"))
          .isInstanceOf(AppException.class)
          .hasMessage("New password and confirmation are required");
    }

    @Test
    @DisplayName("Should throw exception when confirm password is null")
    void shouldThrowExceptionWhenConfirmPasswordIsNull() {
      assertThatThrownBy(() -> userService.changePassword(1L, "oldPassword", "newPassword", null))
          .isInstanceOf(AppException.class)
          .hasMessage("New password and confirmation are required");
    }

    @Test
    @DisplayName("Should throw exception when new password is too short")
    void shouldThrowExceptionWhenNewPasswordTooShort() {

      assertThatThrownBy(() -> userService.changePassword(1L, "oldPassword", "short", "short"))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("New password must be at least 6 characters");
    }

    @Test
    @DisplayName(
        "Should throw exception and stop before loading user when new passwords do not match")
    void shouldThrowExceptionWhenPasswordsDoNotMatch() {
      assertThatThrownBy(() -> userService.changePassword(1L, "old", "new123", "different123"))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("New password and confirmation do not match");

      verify(userRepository, never()).findById(1L);
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(
              () ->
                  userService.changePassword(
                      999L, "oldPassword", "newPassword123", "newPassword123"))
          .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("Should throw exception when current password is incorrect")
    void shouldThrowExceptionWhenCurrentPasswordIncorrect() {
      // Given
      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));
      when(passwordEncoder.matches("wrongPassword", sampleAdmin.getPassword())).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> userService.changePassword(1L, "wrongPassword", "new123", "new123"))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("Current password is incorrect");
    }
  }

  // ========== DELETE USER TESTS ==========

  // Fix for UserServiceTest - HardDeleteUserTests section
  // Replace the entire HardDeleteUserTests class with this:

  // File: src/test/java/com/minimartph/Minit/service/UserServiceTest.java
  // Only the HardDeleteUserTests section - replace this entire nested class

  @Nested
  @DisplayName("hardDeleteUser()")
  class HardDeleteUserTests {

    @Test
    @DisplayName("Should delete cashier user successfully")
    void shouldDeleteUserSuccessfully() {
      // Given - Deleting a CASHIER (not ADMIN)
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin"))
          .thenReturn(Optional.of(sampleAdmin));
      when(userRepository.findById(2L))
          .thenReturn(Optional.of(sampleCashier)); // sampleCashier has role CASHIER
      // DO NOT mock countByRoleAndActiveTrue - it won't be called for non-admin users
      when(saleService.hasSales(2L)).thenReturn(false);

      // When
      userService.hardDeleteUser(2L);

      // Then
      verify(userRepository).deleteById(2L);
      verify(saleService).hasSales(2L);
      // Verify countByRoleAndActiveTrue was NEVER called
      verify(userRepository, never()).countByRoleAndActiveTrue(any());
    }

    @Test
    @DisplayName("Should delete admin user successfully when multiple admins exist")
    void shouldDeleteAdminUserSuccessfullyWhenMultipleAdmins() {
      // Given - Deleting an ADMIN when there are multiple admins
      User anotherAdmin = new User();
      anotherAdmin.setId(3L);
      anotherAdmin.setUsername("admin2");
      anotherAdmin.setRole(Role.ADMIN);
      anotherAdmin.setActive(true);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin"))
          .thenReturn(Optional.of(sampleAdmin));
      when(userRepository.findById(3L)).thenReturn(Optional.of(anotherAdmin));
      // For ADMIN users, countActiveAdmins IS called
      when(userRepository.countByRoleAndActiveTrue(Role.ADMIN)).thenReturn(2L);
      when(saleService.hasSales(3L)).thenReturn(false);

      // When
      userService.hardDeleteUser(3L);

      // Then
      verify(userRepository).deleteById(3L);
      verify(userRepository).countByRoleAndActiveTrue(Role.ADMIN);
      verify(saleService).hasSales(3L);
    }

    @Test
    @DisplayName("Should throw exception when trying to delete own account")
    void shouldThrowExceptionWhenDeletingOwnAccount() {
      // Given
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin"))
          .thenReturn(Optional.of(sampleAdmin));
      when(userRepository.findById(1L)).thenReturn(Optional.of(sampleAdmin));

      // When & Then
      assertThatThrownBy(() -> userService.hardDeleteUser(1L))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("You cannot delete your own account");

      verify(userRepository, never()).deleteById(anyLong());
      verify(userRepository, never()).countByRoleAndActiveTrue(any());
      verify(saleService, never()).hasSales(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when deleting last admin")
    void shouldThrowExceptionWhenDeletingLastAdmin() {
      // Given
      User anotherAdmin = new User();
      anotherAdmin.setId(3L);
      anotherAdmin.setUsername("admin2");
      anotherAdmin.setRole(Role.ADMIN);
      anotherAdmin.setActive(true);

      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin"))
          .thenReturn(Optional.of(sampleAdmin));
      when(userRepository.findById(3L)).thenReturn(Optional.of(anotherAdmin));
      when(userRepository.countByRoleAndActiveTrue(Role.ADMIN))
          .thenReturn(1L); // Only 1 admin total

      // When & Then
      assertThatThrownBy(() -> userService.hardDeleteUser(3L))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("Cannot delete the only remaining admin");

      verify(userRepository, never()).deleteById(anyLong());
      verify(userRepository).countByRoleAndActiveTrue(Role.ADMIN);
      verify(saleService, never()).hasSales(anyLong()); // Exception thrown before sales check
    }

    @Test
    @DisplayName("Should throw exception when user has sales records")
    void shouldThrowExceptionWhenUserHasSales() {
      // Given - Deleting a CASHIER with sales records
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin"))
          .thenReturn(Optional.of(sampleAdmin));
      when(userRepository.findById(2L)).thenReturn(Optional.of(sampleCashier));
      // countByRoleAndActiveTrue is NOT called for CASHIER, so don't mock it
      when(saleService.hasSales(2L)).thenReturn(true); // User has sales

      // When & Then
      assertThatThrownBy(() -> userService.hardDeleteUser(2L))
          .isInstanceOf(AppException.class)
          .hasMessageContaining("Cannot delete user with existing sales");

      verify(userRepository, never()).deleteById(anyLong());
      verify(saleService).hasSales(2L);
      verify(userRepository, never()).countByRoleAndActiveTrue(any());
    }

    @Test
    @DisplayName("Should throw exception when not authenticated")
    void shouldThrowExceptionWhenNotAuthenticated() {
      // Given
      when(securityContext.getAuthentication()).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> userService.hardDeleteUser(2L))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessageContaining("Not authenticated");

      verify(userRepository, never()).findById(anyLong());
      verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when current user not found in database")
    void shouldThrowExceptionWhenCurrentUserNotFound() {
      // Given
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("nonexistent");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("nonexistent")).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.hardDeleteUser(2L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Current user");

      verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when user to delete not found")
    void shouldThrowExceptionWhenUserToDeleteNotFound() {
      // Given
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin"))
          .thenReturn(Optional.of(sampleAdmin));
      when(userRepository.findById(999L)).thenReturn(Optional.empty());

      // When & Then
      assertThatThrownBy(() -> userService.hardDeleteUser(999L))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User not found with id: 999");

      verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when authentication is not authenticated")
    void shouldThrowExceptionWhenAuthenticationNotAuthenticated() {
      // Given
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.isAuthenticated()).thenReturn(false);

      // When & Then
      assertThatThrownBy(() -> userService.hardDeleteUser(2L))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessageContaining("Not authenticated");

      verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should throw exception when authentication object is null")
    void shouldThrowExceptionWhenAuthenticationIsNull() {
      // Given - This tests the auth == null branch of the compound condition (auth == null ||
      // !auth.isAuthenticated())
      when(securityContext.getAuthentication()).thenReturn(null);

      // When & Then
      assertThatThrownBy(() -> userService.hardDeleteUser(2L))
          .isInstanceOf(InvalidCredentialsException.class)
          .hasMessageContaining("Not authenticated");

      verify(userRepository, never()).findById(anyLong());
    }

    @Test
    @DisplayName("Should successfully delete CUSTODIAL user")
    void shouldDeleteCustodialUserSuccessfully() {
      // Given - Deleting a CUSTODIAL user (not ADMIN)
      when(securityContext.getAuthentication()).thenReturn(authentication);
      when(authentication.getName()).thenReturn("admin");
      when(authentication.isAuthenticated()).thenReturn(true);
      when(userRepository.findByUsernameAndActiveTrue("admin"))
          .thenReturn(Optional.of(sampleAdmin));
      when(userRepository.findById(3L))
          .thenReturn(Optional.of(sampleCustodial)); // sampleCustodial has role CUSTODIAL
      when(saleService.hasSales(3L)).thenReturn(false);

      // When
      userService.hardDeleteUser(3L);

      // Then
      verify(userRepository).deleteById(3L);
      verify(saleService).hasSales(3L);
      // Verify countByRoleAndActiveTrue was NEVER called for non-admin
      verify(userRepository, never()).countByRoleAndActiveTrue(any());
    }
  }
}
