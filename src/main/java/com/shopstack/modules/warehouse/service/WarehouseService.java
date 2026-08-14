package com.shopstack.modules.warehouse.service;

import com.shopstack.modules.warehouse.dto.requests.WarehouseRequest;
import com.shopstack.modules.warehouse.dto.responses.WarehouseResponse;
import com.shopstack.modules.warehouse.dto.responses.WarehouseFulfillmentResponse;
import com.shopstack.modules.warehouse.dto.responses.WarehouseAnalyticsResponse;
import com.shopstack.modules.inventory.dto.responses.StockMovementResponse;
import java.util.List;
import java.util.UUID;

public interface WarehouseService {

    WarehouseResponse createWarehouse(WarehouseRequest request);

    WarehouseResponse updateWarehouse(UUID id, WarehouseRequest request);

    WarehouseResponse getWarehouseById(UUID id);

    List<WarehouseResponse> getAllWarehouses();

    void deleteWarehouse(UUID id);

    void allocateInventory(UUID productId, UUID warehouseId, Long vendorId);

    List<WarehouseFulfillmentResponse> allocateOrder(UUID orderId);

    List<WarehouseFulfillmentResponse> getFulfillmentsByWarehouse(UUID warehouseId);

    WarehouseFulfillmentResponse updateFulfillmentStatus(UUID fulfillmentId, String status, UUID staffId);

    WarehouseFulfillmentResponse assignStaff(UUID fulfillmentId, UUID staffId);

    void syncWarehouseStock(UUID warehouseId, UUID productId, Integer newQuantity, UUID performedBy, String note);

    WarehouseAnalyticsResponse getWarehouseAnalytics(UUID warehouseId);

    List<StockMovementResponse> getWarehouseMovements(UUID warehouseId);

    WarehouseFulfillmentResponse prepareShipment(UUID fulfillmentId, String carrier, String trackingNumber);

    WarehouseFulfillmentResponse updateDeliveryStatus(UUID fulfillmentId, String eventType, String location, String description);

    List<com.shopstack.modules.inventory.dto.responses.InventoryResponse> getWarehouseInventory(UUID warehouseId);
}
