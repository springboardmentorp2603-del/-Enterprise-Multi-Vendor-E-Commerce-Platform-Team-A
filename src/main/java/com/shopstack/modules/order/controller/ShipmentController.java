package com.shopstack.modules.order.controller;

import com.shopstack.modules.order.entity.Shipment;
import com.shopstack.modules.order.service.ShipmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/orders")
public class ShipmentController {

    private final ShipmentService shipmentService;

    public ShipmentController(ShipmentService shipmentService) {
        this.shipmentService = shipmentService;
    }

    // Create Shipment Endpoint
    @PostMapping("/{orderId}/shipment")
    public ResponseEntity<Shipment> createShipment(
            @PathVariable UUID orderId,
            @RequestBody CreateShipmentRequest request) {
        Shipment shipment = shipmentService.createShipment(orderId, request.getCarrier(), request.getTrackingNumber());
        return ResponseEntity.ok(shipment);
    }
}

// Inner Request DTO
class CreateShipmentRequest {
    private String carrier;
    private String trackingNumber;

    public String getCarrier() {
        return carrier;
    }
    public void setCarrier(String carrier) {
        this.carrier = carrier;
    }
    public String getTrackingNumber() {
        return trackingNumber;
    }
    public void setTrackingNumber(String trackingNumber) {
        this.trackingNumber = trackingNumber;
    }
}