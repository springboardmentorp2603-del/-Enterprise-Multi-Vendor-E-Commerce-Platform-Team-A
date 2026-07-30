package com.shopstack.modules.coupon.repository;

import com.shopstack.modules.coupon.entity.CouponUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, UUID> {

    long countByCouponId(UUID couponId);

    long countByCouponIdAndUserId(UUID couponId, UUID userId);

    List<CouponUsage> findByCouponIdOrderByCreatedAtDesc(UUID couponId);

    @Query("SELECT COALESCE(SUM(cu.discountApplied), 0) FROM CouponUsage cu WHERE cu.couponId = :couponId")
    BigDecimal sumDiscountByCouponId(@Param("couponId") UUID couponId);
}