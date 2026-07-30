package com.shopstack.modules.pricing.repository;

import com.shopstack.modules.pricing.entity.Discount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface DiscountRepository extends JpaRepository<Discount, UUID> {
    List<Discount> findByVendorId(Long vendorId);
    List<Discount> findByVendorIdAndActiveTrue(Long vendorId);
    List<Discount> findByProduct_Id(UUID productId);
    List<Discount> findByCategory_Id(UUID categoryId);

    @Query("select d from Discount d where d.active = true " +
           "and :now between d.startDate and d.endDate " +
           "and ((d.scopeType = com.shopstack.common.enums.ScopeType.PRODUCT and d.product.id = :productId) " +
           "  or (d.scopeType = com.shopstack.common.enums.ScopeType.CATEGORY and d.category.id = :categoryId))")
    List<Discount> findApplicableDiscounts(@Param("productId") UUID productId,
                                           @Param("categoryId") UUID categoryId,
                                           @Param("now") LocalDateTime now);
}