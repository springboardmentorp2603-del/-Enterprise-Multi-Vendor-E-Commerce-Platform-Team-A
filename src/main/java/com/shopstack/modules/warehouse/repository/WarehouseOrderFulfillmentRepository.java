package com.shopstack.modules.warehouse.repository;

import com.shopstack.modules.warehouse.entity.WarehouseOrderFulfillment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface WarehouseOrderFulfillmentRepository extends JpaRepository<WarehouseOrderFulfillment, UUID> {
    List<WarehouseOrderFulfillment> findByWarehouseId(UUID warehouseId);
    List<WarehouseOrderFulfillment> findByOrderId(UUID orderId);
    Optional<WarehouseOrderFulfillment> findByOrderIdAndWarehouseId(UUID orderId, UUID warehouseId);
}
