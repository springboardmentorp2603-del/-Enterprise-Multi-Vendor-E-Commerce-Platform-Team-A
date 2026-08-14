package com.shopstack.modules.product.entity;

import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "category_commission_rates",
    indexes = {
        @Index(name = "idx_cat_rate_category_id", columnList = "category_id"),
        @Index(name = "idx_cat_rate_active", columnList = "is_active")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCommissionRate extends BaseEntity {

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "effective_from", nullable = false)
    private LocalDateTime effectiveFrom;

    @Column(name = "effective_to")
    private LocalDateTime effectiveTo;
}
