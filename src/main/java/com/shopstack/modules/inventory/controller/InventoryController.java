package com.shopstack.modules.inventory.controller;

import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.inventory.dto.requests.AdjustStockRequest;
import com.shopstack.modules.inventory.dto.requests.InitializeInventoryRequest;
import com.shopstack.modules.inventory.dto.requests.RestockRequest;
import com.shopstack.modules.inventory.dto.responses.InventoryResponse;
import com.shopstack.modules.inventory.dto.responses.StockMovementResponse;
import com.shopstack.modules.inventory.service.InventoryService;
import com.shopstack.modules.vendor.entity.Vendor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final SecurityUtil securityUtil;

    // Reserve/commit/release are intentionally NOT exposed here — they're
    // called directly from the Checkout and Payment-webhook services, not over
    // HTTP.

    // ---- Setup ----

    @PostMapping
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public InventoryResponse initializeInventory(@Valid @RequestBody InitializeInventoryRequest request) {
        Long vendorId = securityUtil.isCurrentUserAdmin()
                ? request.getVendorId()
                : securityUtil.getCurrentVendor().getVendorId();

        return inventoryService.initializeInventory(
                request.getProductId(), vendorId,
                request.getInitialStock(), request.getReorderThreshold());
    }

    // ---- Vendor: manage own stock ----

    @PostMapping("/{productId}/restock")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public InventoryResponse restock(@PathVariable UUID productId, @Valid @RequestBody RestockRequest request) {
        UUID performedBy = securityUtil.getCurrentUser().getId();

        if (securityUtil.isCurrentUserAdmin()) {
            return inventoryService.restock(productId, request.getQuantity(), performedBy, request.getNote());
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        return inventoryService.restock(productId, request.getQuantity(), performedBy,
                request.getNote(), vendor.getVendorId());
    }

    @PostMapping("/{productId}/adjust")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public InventoryResponse adjustStock(@PathVariable UUID productId, @Valid @RequestBody AdjustStockRequest request) {
        UUID performedBy = securityUtil.getCurrentUser().getId();

        if (securityUtil.isCurrentUserAdmin()) {
            return inventoryService.adjustStock(productId, request.getDelta(), performedBy, request.getNote());
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        return inventoryService.adjustStock(productId, request.getDelta(), performedBy,
                request.getNote(), vendor.getVendorId());
    }

    @GetMapping("/{productId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public InventoryResponse getInventory(@PathVariable UUID productId) {
        if (securityUtil.isCurrentUserAdmin()) {
            return inventoryService.getByProductId(productId);
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        return inventoryService.getByProductId(productId, vendor.getVendorId());
    }

    @GetMapping("/{productId}/history")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public List<StockMovementResponse> getHistory(@PathVariable UUID productId) {
        if (securityUtil.isCurrentUserAdmin()) {
            return inventoryService.getHistory(productId);
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        return inventoryService.getHistory(productId, vendor.getVendorId());
    }

    // A vendor's own full inventory list (Vendor Dashboard -> Inventory reports)
    @GetMapping("/my-inventory")
    @PreAuthorize("hasRole('VENDOR')")
    public List<InventoryResponse> getMyInventory() {
        Vendor vendor = securityUtil.getCurrentVendor();
        return inventoryService.getByVendor(vendor.getVendorId());
    }

    @GetMapping("/my-inventory/low-stock")
    @PreAuthorize("hasRole('VENDOR')")
    public List<InventoryResponse> getMyLowStock() {
        Vendor vendor = securityUtil.getCurrentVendor();
        return inventoryService.getLowStock(vendor.getVendorId());
    }

    // ---- Admin: marketplace-wide visibility ----

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public List<InventoryResponse> getByVendor(@PathVariable Long vendorId) {
        return inventoryService.getByVendor(vendorId);
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasRole('ADMIN')")
    public List<InventoryResponse> getAllLowStock() {
        return inventoryService.getAllLowStock();
    }
}