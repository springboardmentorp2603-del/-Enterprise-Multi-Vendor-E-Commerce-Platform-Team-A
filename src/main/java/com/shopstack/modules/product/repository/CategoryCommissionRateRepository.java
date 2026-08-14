package com.shopstack.modules.product.repository;

import com.shopstack.modules.product.entity.CategoryCommissionRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CategoryCommissionRateRepository extends JpaRepository<CategoryCommissionRate, UUID> {

    List<CategoryCommissionRate> findByCategoryId(UUID categoryId);

    @Query("SELECT r FROM CategoryCommissionRate r WHERE r.categoryId = :categoryId AND r.active = true " +
           "AND r.effectiveFrom <= :date AND (r.effectiveTo IS NULL OR r.effectiveTo >= :date) " +
           "ORDER BY r.effectiveFrom DESC")
    List<CategoryCommissionRate> findActiveRateAtDate(
            @Param("categoryId") UUID categoryId,
            @Param("date") LocalDateTime date);
}
