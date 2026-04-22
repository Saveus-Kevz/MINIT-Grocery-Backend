package com.minimartph.Minit.dto;

import com.minimartph.Minit.enums.Gender;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class UserUpdateRequest {
  @Size(min = 1, max = 50, message = "First name must be between 1 and 50 characters")
  private String firstName;

  @Size(min = 1, max = 50, message = "Last name must be between 1 and 50 characters")
  private String lastName;

  @Email(message = "Invalid email format")
  private String email;

  @Pattern(regexp = "^[0-9]{10,11}$", message = "Phone number must be 10-11 digits")
  private String phoneNumber;

  private String address;

  @Pattern(regexp = "^[0-9]+$", message = "Age must be a number")
  private String age;

  private Gender gender;

  @Pattern(
      regexp = "ADMIN|CASHIER|CUSTODIAL",
      message = "Role must be ADMIN, CASHIER, or CUSTODIAL")
  private String role;

  private Boolean active;
  private String photoUrl;
  private String resumeUrl;
  private String barangayClearanceUrl;
}
