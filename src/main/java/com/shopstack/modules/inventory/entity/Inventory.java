package com.shopstack.modules.inventory.entity;

import com.shopstack.common.audit.BaseEntity;
import com.shopstack.modules.product.entity.Product;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Inventory extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false, unique = true)
    private Product product;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Builder.Default
    @Column(name = "available_stock", nullable = false)
    private Integer availableStock = 0;

    @Builder.Default
    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock = 0;

    @Builder.Default
    @Column(name = "reorder_threshold", nullable = false)
    private Integer reorderThreshold = 10;

    // Nullable until the Warehouse Management module (Milestone 3) exists.
    @Column(name = "warehouse_id")
    private UUID warehouseId;

    @Transient
    public boolean isLowStock() {
        return availableStock != null && availableStock <= reorderThreshold;
    }
}
