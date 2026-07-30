package com.shopstack.modules.pricing.entity;

import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "price_history")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PriceHistory extends BaseEntity {

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    // Null on the very first price ever set for a product.
    @Column(name = "old_price", precision = 10, scale = 2)
    private BigDecimal oldPrice;

    @Column(name = "new_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal newPrice;

    // Null when the system made the change rather than a human.
    @Column(name = "changed_by")
    private UUID changedBy;

    @Column(name = "note", length = 255)
    private String note;
}
