package com.minimartph.Minit.repository;

import com.minimartph.Minit.entity.Product;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
  Optional<Product> findByBarcode(String barcode);

  List<Product> findByNameContainingIgnoreCase(String name);

  Page<Product> findByCategory(String category, Pageable pageable);

  boolean existsByNameIgnoreCase(String name);

  boolean existsByBarcode(String barcode);
}
