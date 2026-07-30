package com.shopstack.modules.order.service;

import com.shopstack.common.enums.OrderStatus;
import com.shopstack.common.enums.ShipmentStatus;
import com.shopstack.common.enums.TrackingEventType;
import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.entity.Shipment;
import com.shopstack.modules.order.entity.ShippingAddress;
import com.shopstack.modules.order.entity.TrackingEvent;
import com.shopstack.modules.order.repository.ShipmentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class ShipmentService {
    
    private final ShipmentRepository shipmentRepository;
    private final OrderService orderService;
    
    // =========================================
    // 1. CREATE SHIPMENT
    // =========================================
    @Transactional
    public Shipment createShipment(UUID orderId, String carrier, String trackingNumber) {
        
        log.info("📦 Creating shipment for order: {}", orderId);
        
        // Verify order exists
        Order order = orderService.getOrderById(orderId);
        
        // Create shipment
        Shipment shipment = new Shipment();
        shipment.setOrderId(String.valueOf(orderId));
        shipment.setTrackingNumber(trackingNumber);
        shipment.setCarrier(carrier);
        shipment.setStatus(ShipmentStatus.LABEL_CREATED);
        shipment.setLabelCreatedAt(LocalDateTime.now());
        shipment.setToAddress(formatAddress(order.getShippingAddress()));
        
        Shipment savedShipment = shipmentRepository.save(shipment);
        
        // Update order status to SHIPPED
        orderService.updateOrderStatus(orderId, OrderStatus.SHIPPED,
            "Shipment created with tracking: " + trackingNumber, "System");
        
        log.info("✅ Shipment created: {}", trackingNumber);
        return savedShipment;
    }
    
    // =========================================
    // 2. UPDATE TRACKING
    // =========================================
    @Transactional
    public Shipment updateTracking(String trackingNumber, TrackingEvent event) {
        
        log.info("🔄 Updating tracking for: {}", trackingNumber);
        
        Shipment shipment = shipmentRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new RuntimeException("Shipment not found"));
        
        // Add tracking event
        event.setShipment(shipment);
        event.setTrackingNumber(trackingNumber);
        event.setEventTime(LocalDateTime.now());
        
        // Add to shipment's events list
        if (shipment.getTrackingEvents() == null) {
            shipment.setTrackingEvents(new java.util.ArrayList<>());
        }
        shipment.getTrackingEvents().add(event);
        
        // Update shipment status based on event
        updateShipmentStatus(shipment, event.getEventType());
        
        Shipment updatedShipment = shipmentRepository.save(shipment);
        
        // Update order status based on shipment status
        updateOrderStatusBasedOnShipment(updatedShipment);
        
        log.info("✅ Tracking updated: {}", trackingNumber);
        return updatedShipment;
    }
    
    // =========================================
    // 3. GET SHIPMENT BY ORDER
    // =========================================
    public Shipment getShipmentByOrder(String orderId) {
        log.info("🔍 Fetching shipment for order: {}", orderId);
        return shipmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new RuntimeException("Shipment not found for order"));
    }
    
    // =========================================
    // 4. GET SHIPMENT BY TRACKING
    // =========================================
    public Shipment getShipmentByTracking(String trackingNumber) {
        log.info("🔍 Fetching shipment by tracking: {}", trackingNumber);
        return shipmentRepository.findByTrackingNumber(trackingNumber)
            .orElseThrow(() -> new RuntimeException("Shipment not found"));
    }
    
    // =========================================
    // 5. GET ALL ACTIVE SHIPMENTS
    // =========================================
    public List<Shipment> getActiveShipments() {
        List<ShipmentStatus> activeStatuses = List.of(
            ShipmentStatus.LABEL_CREATED,
            ShipmentStatus.PICKED_UP,
            ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.OUT_FOR_DELIVERY
        );
        return shipmentRepository.findByStatusIn(activeStatuses);
    }
    
    // =========================================
    // HELPER METHODS
    // =========================================
    
    private void updateShipmentStatus(Shipment shipment, TrackingEventType eventType) {
        ShipmentStatus newStatus = null;
        
        switch (eventType) {
            case LABEL_CREATED -> newStatus = ShipmentStatus.LABEL_CREATED;
            case PICKED_UP -> newStatus = ShipmentStatus.PICKED_UP;
            case IN_TRANSIT -> newStatus = ShipmentStatus.IN_TRANSIT;
            case OUT_FOR_DELIVERY -> newStatus = ShipmentStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> newStatus = ShipmentStatus.DELIVERED;
            case EXCEPTION -> newStatus = ShipmentStatus.EXCEPTION;
            default -> newStatus = ShipmentStatus.IN_TRANSIT;
        }
        
        shipment.setStatus(newStatus);
        
        // Update specific timestamps
        if (newStatus == ShipmentStatus.PICKED_UP) {
            shipment.setPickedUpAt(LocalDateTime.now());
        } else if (newStatus == ShipmentStatus.IN_TRANSIT) {
            shipment.setInTransitAt(LocalDateTime.now());
        } else if (newStatus == ShipmentStatus.OUT_FOR_DELIVERY) {
            shipment.setOutForDeliveryAt(LocalDateTime.now());
        } else if (newStatus == ShipmentStatus.DELIVERED) {
            shipment.setDeliveredAt(LocalDateTime.now());
        }
    }
    
    private void updateOrderStatusBasedOnShipment(Shipment shipment) {
        OrderStatus orderStatus = null;
        
        switch (shipment.getStatus()) {
            case LABEL_CREATED -> orderStatus = OrderStatus.PROCESSING;
            case PICKED_UP -> orderStatus = OrderStatus.SHIPPED;
            case IN_TRANSIT -> orderStatus = OrderStatus.IN_TRANSIT;
            case OUT_FOR_DELIVERY -> orderStatus = OrderStatus.OUT_FOR_DELIVERY;
            case DELIVERED -> orderStatus = OrderStatus.DELIVERED;
            case EXCEPTION -> orderStatus = OrderStatus.PROCESSING; // Keep processing
        }
        
        if (orderStatus != null) {
            orderService.updateOrderStatus(
                    UUID.fromString(shipment.getOrderId()),
                orderStatus,
                "Shipment status updated to: " + shipment.getStatus(),
                "System"
            );
        }
    }
    
    private String formatAddress(ShippingAddress address) {
        return address.getStreet() + ", " + 
               address.getCity() + ", " + 
               address.getState() + " " + 
               address.getZipCode() + ", " + 
               address.getCountry();
    }
}