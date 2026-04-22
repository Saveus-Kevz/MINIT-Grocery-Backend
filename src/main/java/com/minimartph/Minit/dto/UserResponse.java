package com.minimartph.Minit.dto;

import com.minimartph.Minit.enums.Gender;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class UserResponse {
  private Long id;
  private String firstName;
  private String lastName;
  private String fullName;
  private String email;
  private Gender gender;
  private String phoneNumber;
  private String address;
  private String age;
  private String username;
  private String role;
  private boolean active;
  private LocalDateTime createdDateTime;
  private String photoUrl;
  private String resumeUrl;
  private String barangayClearanceUrl;
}
