package com.minimartph.Minit.entity;

import com.minimartph.Minit.enums.Gender;
import com.minimartph.Minit.enums.Role;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @CreationTimestamp
  @Column(updatable = false)
  private LocalDateTime createdDateTime;

  @Column(nullable = false, length = 20)
  private String firstName;

  @Column(nullable = false, length = 20)
  private String lastName;

  @Column(nullable = false, length = 50)
  private String fullName;

  @Column(unique = true, nullable = false, length = 100)
  private String email;

  @Enumerated(EnumType.STRING) //
  @Column(nullable = false, length = 10)
  private Gender gender; //

  @Column(nullable = false, length = 20)
  private String phoneNumber;

  @Column(nullable = false, length = 255)
  private String address;

  @Column(nullable = false, length = 10)
  private String age;

  @Column(nullable = false, unique = true, length = 20)
  private String username;

  @Column(nullable = false, length = 255)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private Role role; // ADMIN, CASHIER - ADMIN is also a CASHIER

  @Column(nullable = false)
  private boolean active = true;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String photoUrl;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String resumeUrl;

  @Lob
  @Column(columnDefinition = "LONGTEXT")
  private String barangayClearanceUrl;
}
