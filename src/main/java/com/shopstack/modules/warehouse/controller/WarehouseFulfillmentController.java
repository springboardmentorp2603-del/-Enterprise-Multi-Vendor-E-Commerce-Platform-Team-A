package com.shopstack.modules.warehouse.controller;

import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.warehouse.dto.responses.WarehouseFulfillmentResponse;
import com.shopstack.modules.warehouse.service.WarehouseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouse-fulfillments")
@RequiredArgsConstructor
public class WarehouseFulfillmentController {

    private final WarehouseService warehouseService;
    private final SecurityUtil securityUtil;

    @PostMapping("/allocate/{orderId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF', 'VENDOR')")
    public ResponseEntity<List<WarehouseFulfillmentResponse>> allocateOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(warehouseService.allocateOrder(orderId));
    }

    @GetMapping("/warehouse/{warehouseId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<List<WarehouseFulfillmentResponse>> getFulfillmentsByWarehouse(@PathVariable UUID warehouseId) {
        return ResponseEntity.ok(warehouseService.getFulfillmentsByWarehouse(warehouseId));
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<WarehouseFulfillmentResponse> updateStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        UUID staffId = securityUtil.getCurrentUser().getId();
        return ResponseEntity.ok(warehouseService.updateFulfillmentStatus(id, status, staffId));
    }

    @PutMapping("/{id}/assign")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<WarehouseFulfillmentResponse> assignStaff(@PathVariable UUID id) {
        UUID staffId = securityUtil.getCurrentUser().getId();
        return ResponseEntity.ok(warehouseService.assignStaff(id, staffId));
    }

    @PostMapping("/{id}/prepare-shipment")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<WarehouseFulfillmentResponse> prepareShipment(
            @PathVariable UUID id,
            @RequestParam String carrier,
            @RequestParam String trackingNumber) {
        return ResponseEntity.ok(warehouseService.prepareShipment(id, carrier, trackingNumber));
    }

    @PostMapping("/{id}/delivery-status")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<WarehouseFulfillmentResponse> updateDeliveryStatus(
            @PathVariable UUID id,
            @RequestParam String eventType,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String description) {
        return ResponseEntity.ok(warehouseService.updateDeliveryStatus(id, eventType, location, description));
    }
}
