package com.minimartph.Minit.config;

import com.minimartph.Minit.entity.User;
import com.minimartph.Minit.enums.Gender;
import com.minimartph.Minit.enums.Role;
import com.minimartph.Minit.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

  @Autowired private UserRepository userRepository;

  @Autowired private BCryptPasswordEncoder passwordEncoder;

  @Override
  public void run(String... args) throws Exception {
    // Create admin if no users exist
    if (userRepository.count() == 0) {
      User admin = new User();
      admin.setUsername("admin");
      admin.setPassword(passwordEncoder.encode("admin123"));
      admin.setRole(Role.ADMIN);
      admin.setFirstName("System");
      admin.setLastName("Admin");
      admin.setFullName("System Admin");
      admin.setEmail("admin@minit.com");
      admin.setAddress("Default Address");
      admin.setPhoneNumber("09123456789");
      admin.setAge("30");
      admin.setGender(Gender.MALE);
      admin.setActive(true);
      userRepository.save(admin);
      System.out.println("Default admin created with ID: " + admin.getId());
    }

    // Create a cashier if none exists with role CASHIER
    if (userRepository.findByRole(Role.CASHIER).isEmpty()) {
      User cashier = new User();
      cashier.setUsername("cashier");
      cashier.setPassword(passwordEncoder.encode("cashier123"));
      cashier.setRole(Role.CASHIER);
      cashier.setFirstName("Default");
      cashier.setLastName("Cashier");
      cashier.setFullName("Default Cashier");
      cashier.setEmail("cashier@minit.com");
      cashier.setAddress("Default Address");
      cashier.setPhoneNumber("09123456788");
      cashier.setAge("25");
      cashier.setGender(Gender.MALE);
      cashier.setActive(true);
      userRepository.save(cashier);
      System.out.println("Default cashier created with ID: " + cashier.getId());
    }
  }
}