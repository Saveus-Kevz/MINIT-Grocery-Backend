package com.minimartph.Minit.repository;

import com.minimartph.Minit.entity.User;
import com.minimartph.Minit.enums.Role;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(String username);

  boolean existsByUsername(String username);

  // ✅ These MUST take Role enum, NOT String
  List<User> findByRole(Role role);

  long countByRole(Role role);

  boolean existsByEmail(String email);

  Optional<User> findByUsernameAndActiveTrue(String username);

  Optional<User> findByIdAndActiveTrue(Long id);

  List<User> findAllByActiveTrue();

  // ✅ These MUST take Role enum, NOT String
  List<User> findByRoleAndActiveTrue(Role role);

  long countByRoleAndActiveTrue(Role role);
}
