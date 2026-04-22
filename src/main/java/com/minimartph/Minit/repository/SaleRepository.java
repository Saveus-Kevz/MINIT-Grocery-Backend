package com.minimartph.Minit.repository;

import com.minimartph.Minit.entity.Sale;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleRepository extends JpaRepository<Sale, Long> {
  List<Sale> findByCashierId(Long cashierId);

  List<Sale> findByCashierIdAndSaleDateBetween(
      Long cashierId, LocalDateTime start, LocalDateTime end);

  List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);

  @Query(
      value =
          "SELECT DATE(s.sale_date) as sale_date, SUM(s.total_amount) as total_amount "
              + "FROM sales s WHERE s.sale_date BETWEEN :start AND :end GROUP BY DATE(s.sale_date)",
      nativeQuery = true)
  List<Object[]> getTotalSalesByDateRangeNative(
      @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

  boolean existsByCashierId(Long userId);
}
