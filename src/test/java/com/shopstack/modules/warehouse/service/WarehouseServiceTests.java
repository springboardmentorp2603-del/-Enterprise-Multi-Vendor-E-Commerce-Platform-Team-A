package com.shopstack.modules.warehouse.service;

import com.shopstack.common.exception.ConflictException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.inventory.entity.Inventory;
import com.shopstack.modules.inventory.repository.InventoryRepository;
import com.shopstack.modules.warehouse.dto.requests.WarehouseRequest;
import com.shopstack.modules.warehouse.dto.responses.WarehouseResponse;
import com.shopstack.modules.warehouse.entity.Warehouse;
import com.shopstack.modules.warehouse.mapper.WarehouseMapper;
import com.shopstack.modules.warehouse.repository.WarehouseRepository;
import com.shopstack.modules.warehouse.repository.WarehouseOrderFulfillmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTests {

    @Mock
    private WarehouseRepository warehouseRepository;
    @Mock
    private WarehouseOrderFulfillmentRepository fulfillmentRepository;
    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private WarehouseMapper mapper;

    @InjectMocks
    private WarehouseServiceImpl warehouseService;

    private Warehouse warehouse;
    private WarehouseRequest request;
    private WarehouseResponse response;
    private UUID warehouseId;

    @BeforeEach
    void setUp() {
        warehouseId = UUID.randomUUID();
        warehouse = Warehouse.builder()
                .code("WH-001")
                .name("Seattle East")
                .address("123 Main St")
                .city("Seattle")
                .state("WA")
                .zipCode("98101")
                .country("USA")
                .capacity(1000)
                .status("ACTIVE")
                .build();
        warehouse.setId(warehouseId);

        request = WarehouseRequest.builder()
                .code("WH-001")
                .name("Seattle East")
                .address("123 Main St")
                .city("Seattle")
                .state("WA")
                .zipCode("98101")
                .country("USA")
                .capacity(1000)
                .status("ACTIVE")
                .build();

        response = WarehouseResponse.builder()
                .id(warehouseId)
                .code("WH-001")
                .name("Seattle East")
                .capacity(1000)
                .status("ACTIVE")
                .build();
    }

    @Test
    void testCreateWarehouseSuccess() {
        when(warehouseRepository.findByCode("WH-001")).thenReturn(Optional.empty());
        when(mapper.toEntity(any(WarehouseRequest.class))).thenReturn(warehouse);
        when(warehouseRepository.save(any(Warehouse.class))).thenReturn(warehouse);
        when(mapper.toResponse(any(Warehouse.class))).thenReturn(response);

        WarehouseResponse created = warehouseService.createWarehouse(request);

        assertNotNull(created);
        assertEquals("WH-001", created.getCode());
        verify(warehouseRepository, times(1)).save(any(Warehouse.class));
    }

    @Test
    void testCreateWarehouseDuplicateCodeThrowsConflictException() {
        when(warehouseRepository.findByCode("WH-001")).thenReturn(Optional.of(warehouse));

        assertThrows(ConflictException.class, () -> warehouseService.createWarehouse(request));
        verify(warehouseRepository, never()).save(any(Warehouse.class));
    }

    @Test
    void testGetWarehouseByIdNotFoundThrowsResourceNotFoundException() {
        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> warehouseService.getWarehouseById(warehouseId));
    }

    @Test
    void testAllocateInventoryExceedsCapacityThrowsConflictException() {
        UUID productId = UUID.randomUUID();
        Inventory inventory = Inventory.builder()
                .availableStock(600)
                .reservedStock(500)
                .build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProduct_Id(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.findByWarehouseId(warehouseId)).thenReturn(Collections.emptyList());

        assertThrows(ConflictException.class, () -> warehouseService.allocateInventory(productId, warehouseId, null));
    }

    @Test
    void testAllocateInventorySuccess() {
        UUID productId = UUID.randomUUID();
        Inventory inventory = Inventory.builder()
                .availableStock(100)
                .reservedStock(50)
                .build();

        when(warehouseRepository.findById(warehouseId)).thenReturn(Optional.of(warehouse));
        when(inventoryRepository.findByProduct_Id(productId)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.findByWarehouseId(warehouseId)).thenReturn(Collections.emptyList());

        warehouseService.allocateInventory(productId, warehouseId, null);

        assertEquals(warehouseId, inventory.getWarehouseId());
        verify(inventoryRepository, times(1)).save(inventory);
    }
}
