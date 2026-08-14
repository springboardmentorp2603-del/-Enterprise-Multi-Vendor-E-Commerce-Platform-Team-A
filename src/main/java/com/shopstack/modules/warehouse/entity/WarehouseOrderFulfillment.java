package com.shopstack.modules.warehouse.entity;

import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.UUID;

@Entity
@Table(name = "warehouse_fulfillments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseOrderFulfillment extends BaseEntity {

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(nullable = false)
    private String status; // ALLOCATED, PICKING, PACKED, READY_FOR_SHIPMENT, SHIPPED, CANCELLED

    @Column(name = "assigned_staff_id")
    private UUID assignedStaffId;

    @Column(name = "tracking_number")
    private String trackingNumber;

    private String carrier;

    private String notes;
}
