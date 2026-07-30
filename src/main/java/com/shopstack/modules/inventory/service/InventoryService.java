package com.shopstack.modules.inventory.service;

import com.shopstack.modules.inventory.dto.responses.InventoryResponse;
import com.shopstack.modules.inventory.dto.responses.StockMovementResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    // ---- Admin: unrestricted lookups ----
    InventoryResponse getByProductId(UUID productId);

    List<InventoryResponse> getByVendor(Long vendorId);

    List<InventoryResponse> getLowStock(Long vendorId);

    List<InventoryResponse> getAllLowStock();

    List<StockMovementResponse> getHistory(UUID productId);

    // ---- Vendor-scoped overloads: throw ForbiddenException if the product's
    // inventory does not belong to vendorId ----
    InventoryResponse getByProductId(UUID productId, Long vendorId);

    List<StockMovementResponse> getHistory(UUID productId, Long vendorId);

    /** Called when a product is approved, to give it a stock row to work with. */
    InventoryResponse initializeInventory(UUID productId, Long vendorId,
            int initialStock, int reorderThreshold);

    /** Vendor adds physical stock. */
    InventoryResponse restock(UUID productId, int quantity, UUID performedBy, String note);

    InventoryResponse restock(UUID productId, int quantity, UUID performedBy, String note, Long vendorId);

    /** Vendor/admin manual correction; delta may be positive or negative. */
    InventoryResponse adjustStock(UUID productId, int delta, UUID performedBy, String note);

    InventoryResponse adjustStock(UUID productId, int delta, UUID performedBy, String note, Long vendorId);

    /**
     * Phase 1 of checkout: moves quantity from available -> reserved for one
     * product line. Throws ConflictException if not enough stock is available.
     * Internal use only (called by the Checkout service) — not exposed over HTTP.
     */
    void reserveStock(UUID productId, int quantity, UUID orderId, UUID performedBy);

    /**
     * Phase 2a, on payment success: finalizes all reservations for an order,
     * removing them from reservedStock permanently. Idempotent. Internal use only.
     */
    void commitStock(UUID orderId, UUID performedBy);

    /**
     * Phase 2b, on payment failure/cancellation: returns all reservations
     * for an order back to availableStock. Idempotent. Internal use only.
     */
    void releaseStock(UUID orderId, UUID performedBy);
}