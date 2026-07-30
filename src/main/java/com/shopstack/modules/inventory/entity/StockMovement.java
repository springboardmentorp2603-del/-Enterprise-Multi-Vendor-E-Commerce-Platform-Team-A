package com.shopstack.modules.inventory.entity;

import com.shopstack.common.audit.BaseEntity;
import com.shopstack.common.enums.MovementType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StockMovement extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private Inventory inventory;

    // Denormalized for reporting/export queries that shouldn't have to
    // join through inventory just to filter by product or vendor.
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 20)
    private MovementType movementType;

    @Column(name = "change_qty", nullable = false)
    private Integer changeQty;

    @Column(name = "previous_stock", nullable = false)
    private Integer previousStock;

    @Column(name = "new_stock", nullable = false)
    private Integer newStock;

    // Set for SALE / RESERVATION / RELEASE / RETURN movements. Left as a
    // raw id (no @ManyToOne to Order) since the Order module doesn't
    // exist yet and this table must stay valid regardless of it.
    @Column(name = "reference_order_id")
    private UUID referenceOrderId;

    // Null when the movement was made by the system (e.g. auto-release
    // on payment failure) rather than a human action.
    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "note", length = 255)
    private String note;
}