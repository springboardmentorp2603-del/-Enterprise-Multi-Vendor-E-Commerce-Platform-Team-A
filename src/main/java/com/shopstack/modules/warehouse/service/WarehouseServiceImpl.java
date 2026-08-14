package com.shopstack.modules.warehouse.service;

import com.shopstack.common.exception.BadRequestException;
import com.shopstack.common.exception.ConflictException;
import com.shopstack.common.exception.ForbiddenException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.inventory.dto.responses.StockMovementResponse;
import com.shopstack.modules.inventory.entity.Inventory;
import com.shopstack.modules.inventory.mapper.InventoryMapper;
import com.shopstack.modules.inventory.repository.InventoryRepository;
import com.shopstack.modules.inventory.repository.StockMovementRepository;
import com.shopstack.modules.inventory.service.InventoryService;
import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.entity.OrderItem;
import com.shopstack.modules.order.repository.OrderRepository;
import com.shopstack.modules.order.service.ShipmentService;
import com.shopstack.modules.warehouse.dto.requests.WarehouseRequest;
import com.shopstack.modules.warehouse.dto.responses.WarehouseResponse;
import com.shopstack.modules.warehouse.dto.responses.WarehouseFulfillmentResponse;
import com.shopstack.modules.warehouse.dto.responses.WarehouseAnalyticsResponse;
import com.shopstack.modules.warehouse.entity.Warehouse;
import com.shopstack.modules.warehouse.entity.WarehouseOrderFulfillment;
import com.shopstack.modules.warehouse.mapper.WarehouseMapper;
import com.shopstack.modules.warehouse.repository.WarehouseRepository;
import com.shopstack.modules.warehouse.repository.WarehouseOrderFulfillmentRepository;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.inventory.entity.StockMovement;
import com.shopstack.modules.order.entity.TrackingEvent;
import com.shopstack.modules.order.entity.Shipment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WarehouseServiceImpl implements WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseOrderFulfillmentRepository fulfillmentRepository;
    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final OrderRepository orderRepository;
    private final InventoryService inventoryService;
    private final ShipmentService shipmentService;
    private final WarehouseMapper mapper;
    private final InventoryMapper inventoryMapper;
    private final ProductRepository productRepository;

    @Override
    @Transactional
    public WarehouseResponse createWarehouse(WarehouseRequest request) {
        if (warehouseRepository.findByCode(request.getCode()).isPresent()) {
            throw new ConflictException("Warehouse with code " + request.getCode() + " already exists.");
        }
        Warehouse warehouse = mapper.toEntity(request);
        if (warehouse.getStatus() == null) {
            warehouse.setStatus("ACTIVE");
        }
        Warehouse saved = warehouseRepository.save(warehouse);
        WarehouseResponse response = mapper.toResponse(saved);
        response.setOccupancy(0);
        return response;
    }

    @Override
    @Transactional
    public WarehouseResponse updateWarehouse(UUID id, WarehouseRequest request) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        warehouseRepository.findByCode(request.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(id)) {
                throw new ConflictException("Warehouse with code " + request.getCode() + " already exists.");
            }
        });

        warehouse.setCode(request.getCode());
        warehouse.setName(request.getName());
        warehouse.setAddress(request.getAddress());
        warehouse.setCity(request.getCity());
        warehouse.setState(request.getState());
        warehouse.setZipCode(request.getZipCode());
        warehouse.setCountry(request.getCountry());
        warehouse.setCapacity(request.getCapacity());
        if (request.getStatus() != null) {
            warehouse.setStatus(request.getStatus());
        }

        Warehouse saved = warehouseRepository.save(warehouse);
        WarehouseResponse response = mapper.toResponse(saved);
        response.setOccupancy(calculateOccupancy(id));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseResponse getWarehouseById(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        WarehouseResponse response = mapper.toResponse(warehouse);
        response.setOccupancy(calculateOccupancy(id));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseResponse> getAllWarehouses() {
        List<Warehouse> warehouses = warehouseRepository.findAll();
        return warehouses.stream()
                .map(w -> {
                    WarehouseResponse res = mapper.toResponse(w);
                    res.setOccupancy(calculateOccupancy(w.getId()));
                    return res;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteWarehouse(UUID id) {
        Warehouse warehouse = warehouseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        // Soft delete
        warehouse.setDeleted(true);
        warehouseRepository.save(warehouse);
    }

    @Override
    @Transactional
    public void allocateInventory(UUID productId, UUID warehouseId, Long vendorId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));
        if (!"ACTIVE".equals(warehouse.getStatus())) {
            throw new BadRequestException("Warehouse is not active");
        }

        Inventory inventory = inventoryRepository.findByProduct_Id(productId)
                .orElseGet(() -> {
                    Product product = productRepository.findById(productId)
                            .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
                    
                    int stock = product.getStockQuantity() != null ? product.getStockQuantity() : 0;
                    
                    Inventory newInv = Inventory.builder()
                            .product(product)
                            .vendorId(product.getVendorId())
                            .availableStock(stock)
                            .reservedStock(0)
                            .reorderThreshold(10)
                            .build();
                    
                    Inventory savedInv = inventoryRepository.save(newInv);
                    
                    if (stock > 0) {
                        StockMovement movement = StockMovement.builder()
                                .inventory(savedInv)
                                .productId(productId)
                                .vendorId(product.getVendorId())
                                .movementType(com.shopstack.common.enums.MovementType.RESTOCK)
                                .changeQty(stock)
                                .previousStock(0)
                                .newStock(stock)
                                .note("Initial stock on allocation")
                                .build();
                        stockMovementRepository.save(movement);
                    }
                    
                    return savedInv;
                });

        if (vendorId != null && !inventory.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("This inventory does not belong to the current vendor");
        }

        // Capacity check
        int currentOccupancy = calculateOccupancy(warehouseId);
        int itemStock = (inventory.getAvailableStock() != null ? inventory.getAvailableStock() : 0) +
                        (inventory.getReservedStock() != null ? inventory.getReservedStock() : 0);

        if (currentOccupancy + itemStock > warehouse.getCapacity()) {
            throw new ConflictException("Allocation fails: Exceeds warehouse capacity of " + warehouse.getCapacity());
        }

        inventory.setWarehouseId(warehouseId);
        inventoryRepository.save(inventory);
    }

    @Override
    @Transactional
    public List<WarehouseFulfillmentResponse> allocateOrder(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        List<WarehouseOrderFulfillment> existing = fulfillmentRepository.findByOrderId(orderId);
        if (!existing.isEmpty()) {
            return mapper.toFulfillmentResponseList(existing);
        }

        java.util.Set<UUID> warehouseIds = new java.util.HashSet<>();
        for (OrderItem item : order.getItems()) {
            Inventory inventory = inventoryRepository.findByProduct_Id(UUID.fromString(item.getProductId()))
                    .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product: " + item.getProductId()));

            UUID whId = inventory.getWarehouseId();
            if (whId == null) {
                List<Warehouse> activeWarehouses = warehouseRepository.findAll().stream()
                        .filter(w -> "ACTIVE".equals(w.getStatus()) && !w.getDeleted())
                        .toList();
                if (activeWarehouses.isEmpty()) {
                    throw new BadRequestException("No active warehouses available for dynamic allocation");
                }
                whId = activeWarehouses.get(0).getId();
                inventory.setWarehouseId(whId);
                inventoryRepository.save(inventory);
            }
            warehouseIds.add(whId);
        }

        List<WarehouseOrderFulfillment> savedFulfillments = new java.util.ArrayList<>();
        for (UUID whId : warehouseIds) {
            WarehouseOrderFulfillment fulfillment = WarehouseOrderFulfillment.builder()
                    .orderId(orderId)
                    .warehouseId(whId)
                    .status("ALLOCATED")
                    .build();
            savedFulfillments.add(fulfillmentRepository.save(fulfillment));
        }

        order.setStatus("PROCESSING");
        orderRepository.save(order);

        return mapper.toFulfillmentResponseList(savedFulfillments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WarehouseFulfillmentResponse> getFulfillmentsByWarehouse(UUID warehouseId) {
        return mapper.toFulfillmentResponseList(fulfillmentRepository.findByWarehouseId(warehouseId));
    }

    @Override
    @Transactional
    public WarehouseFulfillmentResponse updateFulfillmentStatus(UUID fulfillmentId, String status, UUID staffId) {
        WarehouseOrderFulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment task not found"));

        String targetStatus = status.toUpperCase();
        List<String> validStatuses = List.of("ALLOCATED", "PICKING", "PACKED", "READY_FOR_SHIPMENT", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(targetStatus)) {
            throw new BadRequestException("Invalid status: " + status);
        }

        fulfillment.setStatus(targetStatus);
        if (staffId != null) {
            fulfillment.setAssignedStaffId(staffId);
        }
        WarehouseOrderFulfillment saved = fulfillmentRepository.save(fulfillment);

        // Bridge to Order status
        try {
            Order order = orderRepository.findById(fulfillment.getOrderId()).orElse(null);
            if (order != null) {
                if ("PICKING".equals(targetStatus)) {
                    order.setStatus("PROCESSING");
                } else if ("PACKED".equals(targetStatus)) {
                    order.setStatus("PACKING");
                } else if ("READY_FOR_SHIPMENT".equals(targetStatus)) {
                    order.setStatus("READY_FOR_PICKUP");
                } else if ("SHIPPED".equals(targetStatus)) {
                    order.setStatus("SHIPPED");
                } else if ("DELIVERED".equals(targetStatus)) {
                    order.setStatus("DELIVERED");
                } else if ("CANCELLED".equals(targetStatus)) {
                    order.setStatus("CANCELLED");
                }
                orderRepository.save(order);
            }
        } catch (Exception ignored) {}

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public WarehouseFulfillmentResponse assignStaff(UUID fulfillmentId, UUID staffId) {
        WarehouseOrderFulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment task not found"));

        fulfillment.setAssignedStaffId(staffId);
        if ("ALLOCATED".equals(fulfillment.getStatus())) {
            fulfillment.setStatus("PICKING");

            // Bridge to Order status
            try {
                Order order = orderRepository.findById(fulfillment.getOrderId()).orElse(null);
                if (order != null) {
                    order.setStatus("PROCESSING");
                    orderRepository.save(order);
                }
            } catch (Exception ignored) {}
        }
        WarehouseOrderFulfillment saved = fulfillmentRepository.save(fulfillment);
        return mapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void syncWarehouseStock(UUID warehouseId, UUID productId, Integer newQuantity, UUID performedBy, String note) {
        Inventory inventory = inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found for product"));

        if (inventory.getWarehouseId() == null || !inventory.getWarehouseId().equals(warehouseId)) {
            throw new BadRequestException("Product inventory is not allocated to the specified warehouse");
        }

        int currentAvailable = inventory.getAvailableStock() != null ? inventory.getAvailableStock() : 0;
        int delta = newQuantity - currentAvailable;

        if (delta != 0) {
            inventoryService.adjustStock(productId, delta, performedBy, note);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public WarehouseAnalyticsResponse getWarehouseAnalytics(UUID warehouseId) {
        Warehouse warehouse = warehouseRepository.findById(warehouseId)
                .orElseThrow(() -> new ResourceNotFoundException("Warehouse not found"));

        int occupancy = calculateOccupancy(warehouseId);
        double occupancyRate = warehouse.getCapacity() > 0 ? (occupancy * 100.0) / warehouse.getCapacity() : 0.0;

        List<WarehouseOrderFulfillment> fulfillments = fulfillmentRepository.findByWarehouseId(warehouseId);
        long activeCount = fulfillments.stream()
                .filter(f -> !"SHIPPED".equals(f.getStatus()) && !"CANCELLED".equals(f.getStatus()))
                .count();

        java.util.Map<String, Long> statusMap = fulfillments.stream()
                .collect(Collectors.groupingBy(
                        WarehouseOrderFulfillment::getStatus,
                        Collectors.counting()
                ));

        List<Inventory> lowStockInventories = inventoryRepository.findByWarehouseId(warehouseId).stream()
                .filter(Inventory::isLowStock)
                .toList();

        return WarehouseAnalyticsResponse.builder()
                .warehouseId(warehouseId)
                .warehouseName(warehouse.getName())
                .totalCapacity(warehouse.getCapacity())
                .currentOccupancy(occupancy)
                .occupancyRate(occupancyRate)
                .activeFulfillmentsCount(activeCount)
                .fulfillmentsByStatus(statusMap)
                .lowStockItemsCount((long) lowStockInventories.size())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getWarehouseMovements(UUID warehouseId) {
        return inventoryMapper.toMovementResponseList(stockMovementRepository.findByWarehouseId(warehouseId));
    }

    @Override
    @Transactional
    public WarehouseFulfillmentResponse prepareShipment(UUID fulfillmentId, String carrier, String trackingNumber) {
        WarehouseOrderFulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment task not found"));

        // Call ShipmentService to generate shipment and set Order state to SHIPPED.
        shipmentService.createShipment(fulfillment.getOrderId(), carrier, trackingNumber);

        fulfillment.setStatus("SHIPPED");
        fulfillment.setCarrier(carrier);
        fulfillment.setTrackingNumber(trackingNumber);
        WarehouseOrderFulfillment saved = fulfillmentRepository.save(fulfillment);
        return mapper.toResponse(saved);
    }

    private int calculateOccupancy(UUID warehouseId) {
        return inventoryRepository.findByWarehouseId(warehouseId).stream()
                .mapToInt(i -> (i.getAvailableStock() != null ? i.getAvailableStock() : 0) +
                               (i.getReservedStock() != null ? i.getReservedStock() : 0))
                .sum();
    }

    @Override
    @Transactional
    public WarehouseFulfillmentResponse updateDeliveryStatus(UUID fulfillmentId, String eventTypeStr, String location, String description) {
        WarehouseOrderFulfillment fulfillment = fulfillmentRepository.findById(fulfillmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Fulfillment task not found"));

        com.shopstack.common.enums.TrackingEventType eventType;
        try {
            eventType = com.shopstack.common.enums.TrackingEventType.valueOf(eventTypeStr.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid tracking event type: " + eventTypeStr);
        }

        Shipment shipment = shipmentService.getShipmentByOrder(String.valueOf(fulfillment.getOrderId()));
        if (shipment == null) {
            throw new ResourceNotFoundException("Shipment details not found for order: " + fulfillment.getOrderId());
        }

        TrackingEvent event = new TrackingEvent();
        event.setEventType(eventType);
        event.setLocation(location != null ? location : "Warehouse Hub");
        event.setDescription(description != null ? description : eventType.getDescription());

        shipmentService.updateTracking(shipment.getTrackingNumber(), event);

        if (eventType == com.shopstack.common.enums.TrackingEventType.DELIVERED) {
            fulfillment.setStatus("DELIVERED");
            fulfillmentRepository.save(fulfillment);
        }

        return mapper.toResponse(fulfillment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<com.shopstack.modules.inventory.dto.responses.InventoryResponse> getWarehouseInventory(UUID warehouseId) {
        return inventoryMapper.toResponseList(inventoryRepository.findByWarehouseId(warehouseId));
    }
}
