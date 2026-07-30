package com.shopstack.modules.pricing.entity;

import com.shopstack.common.audit.BaseEntity;
import com.shopstack.common.enums.DiscountType;
import com.shopstack.common.enums.ScopeType;
import com.shopstack.modules.product.entity.Category;
import com.shopstack.modules.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "discounts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Discount extends BaseEntity {

    // Null for admin-created CATEGORY-scoped discounts (marketplace-wide
    // promotions have no single owning vendor). Always set for
    // PRODUCT-scoped discounts created by a vendor.
    @Column(name = "vendor_id")
    private Long vendorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "scope_type", nullable = false, length = 20)
    private ScopeType scopeType;

    // Exactly one of these two is set, matching scopeType — enforced at
    // the DB level too (chk_scope_exclusive in pricing_schema.sql).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    // 0-100 when discountType = PERCENTAGE, a currency amount when FLAT
    @Column(name = "value", nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(name = "start_date", nullable = false)
    private LocalDateTime startDate;

    @Column(name = "end_date", nullable = false)
    private LocalDateTime endDate;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;

    @Transient
    public boolean isCurrentlyActive() {
        LocalDateTime now = LocalDateTime.now();
        return Boolean.TRUE.equals(active)
                && !now.isBefore(startDate)
                && !now.isAfter(endDate);
    }
}