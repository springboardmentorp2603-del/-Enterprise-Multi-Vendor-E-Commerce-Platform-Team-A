package com.shopstack.modules.order.repository;

import com.shopstack.common.enums.ShipmentStatus;
import com.shopstack.modules.order.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, String> {
    
    // Find shipment by order ID
    Optional<Shipment> findByOrderId(String orderId);
    
    // Find shipment by tracking number
    Optional<Shipment> findByTrackingNumber(String trackingNumber);
    
    // Find shipments by status
    List<Shipment> findByStatus(ShipmentStatus status);
    
    // Find shipments by multiple statuses (for active tracking)
    List<Shipment> findByStatusIn(List<ShipmentStatus> statuses);
}