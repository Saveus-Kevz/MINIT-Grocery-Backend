package com.minimartph.Minit.dto;

import com.minimartph.Minit.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserRegistrationRequest {
  @NotBlank(message = "First name is required")
  @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
  private String firstName;

  @NotBlank(message = "Last name is required")
  @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
  private String lastName;

  private String fullName; // optional

  @NotBlank(message = "Email is required")
  @Email(message = "Invalid email format")
  private String email;

  @NotBlank(message = "Phone number is required")
  @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
  private String phoneNumber;

  @NotNull(message = "Gender is required")
  private Gender gender;

  @NotBlank(message = "Address is required")
  private String address;

  @NotBlank(message = "Age is required")
  @Pattern(regexp = "^[0-9]+$", message = "Age must be a number")
  private String age;

  @NotBlank(message = "Username is required")
  @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
  private String username;

  @NotBlank(message = "Password is required")
  @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
  @Pattern(
          regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,}$",
          message = "Password must contain at least one digit, one lowercase, one uppercase, one special character (@#$%^&+=), and no spaces"
  )
  private String password;

  @NotBlank(message = "Role is required")
  @Pattern(
      regexp = "ADMIN|CASHIER|CUSTODIAL",
      message = "Role must be ADMIN, CASHIER, or CUSTODIAL")
  private String role;

  // Optional fields - no validation needed
  private String photoUrl;
  private String resumeUrl;
  private String barangayClearanceUrl;
}
