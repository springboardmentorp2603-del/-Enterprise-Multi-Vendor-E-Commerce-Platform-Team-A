package com.shopstack.modules.warehouse.controller;

import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.inventory.dto.responses.StockMovementResponse;
import com.shopstack.modules.warehouse.dto.requests.SyncStockRequest;
import com.shopstack.modules.warehouse.dto.requests.WarehouseRequest;
import com.shopstack.modules.warehouse.dto.responses.WarehouseAnalyticsResponse;
import com.shopstack.modules.warehouse.dto.responses.WarehouseResponse;
import com.shopstack.modules.warehouse.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponse> createWarehouse(@Valid @RequestBody WarehouseRequest request) {
        return new ResponseEntity<>(warehouseService.createWarehouse(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF', 'VENDOR')")
    public ResponseEntity<List<WarehouseResponse>> getAllWarehouses() {
        return ResponseEntity.ok(warehouseService.getAllWarehouses());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<WarehouseResponse> getWarehouseById(@PathVariable UUID id) {
        return ResponseEntity.ok(warehouseService.getWarehouseById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WarehouseResponse> updateWarehouse(@PathVariable UUID id, @Valid @RequestBody WarehouseRequest request) {
        return ResponseEntity.ok(warehouseService.updateWarehouse(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWarehouse(@PathVariable UUID id) {
        warehouseService.deleteWarehouse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{warehouseId}/inventory/{productId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")
    public ResponseEntity<Void> allocateInventory(@PathVariable UUID warehouseId, @PathVariable UUID productId) {
        Long vendorId = null;
        if (!securityUtil.isCurrentUserAdmin()) {
            vendorId = securityUtil.getCurrentVendor().getVendorId();
        }
        warehouseService.allocateInventory(productId, warehouseId, vendorId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{warehouseId}/inventory/{productId}/sync")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<Void> syncStock(
            @PathVariable UUID warehouseId,
            @PathVariable UUID productId,
            @Valid @RequestBody SyncStockRequest request) {
        UUID performedBy = securityUtil.getCurrentUser().getId();
        warehouseService.syncWarehouseStock(warehouseId, productId, request.getNewQuantity(), performedBy, request.getNote());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<WarehouseAnalyticsResponse> getWarehouseAnalytics(@PathVariable UUID id) {
        return ResponseEntity.ok(warehouseService.getWarehouseAnalytics(id));
    }

    @GetMapping("/{id}/movements")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<List<StockMovementResponse>> getWarehouseMovements(@PathVariable UUID id) {
        return ResponseEntity.ok(warehouseService.getWarehouseMovements(id));
    }

    @GetMapping("/{id}/inventory")
    @PreAuthorize("hasAnyRole('ADMIN', 'WAREHOUSE_STAFF')")
    public ResponseEntity<List<com.shopstack.modules.inventory.dto.responses.InventoryResponse>> getWarehouseInventory(@PathVariable UUID id) {
        return ResponseEntity.ok(warehouseService.getWarehouseInventory(id));
    }
}
