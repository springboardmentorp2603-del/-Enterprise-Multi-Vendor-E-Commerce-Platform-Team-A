package com.shopstack.modules.inventory.service;

import com.shopstack.common.enums.MovementType;
import com.shopstack.common.exception.BadRequestException;
import com.shopstack.common.exception.ConflictException;
import com.shopstack.common.exception.DuplicateResourceException;
import com.shopstack.common.exception.ForbiddenException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.inventory.dto.responses.InventoryResponse;
import com.shopstack.modules.inventory.dto.responses.StockMovementResponse;
import com.shopstack.modules.inventory.entity.Inventory;
import com.shopstack.modules.inventory.entity.StockMovement;
import com.shopstack.modules.inventory.mapper.InventoryMapper;
import com.shopstack.modules.inventory.repository.InventoryRepository;
import com.shopstack.modules.inventory.repository.StockMovementRepository;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final ProductRepository productRepository;
    private final InventoryMapper inventoryMapper;

    // ---------------------------------------------------------------
    // Reads
    // ---------------------------------------------------------------

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(UUID productId) {
        return inventoryMapper.toResponse(findInventoryOrThrow(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getByProductId(UUID productId, Long vendorId) {
        Inventory inventory = findInventoryOrThrow(productId);
        verifyOwnership(inventory, vendorId);
        return inventoryMapper.toResponse(inventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getByVendor(Long vendorId) {
        return inventoryMapper.toResponseList(inventoryRepository.findByVendorId(vendorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStock(Long vendorId) {
        return inventoryMapper.toResponseList(inventoryRepository.findLowStockByVendor(vendorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getAllLowStock() {
        return inventoryMapper.toResponseList(inventoryRepository.findAllLowStock());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getHistory(UUID productId) {
        return inventoryMapper.toMovementResponseList(
                stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockMovementResponse> getHistory(UUID productId, Long vendorId) {
        Inventory inventory = findInventoryOrThrow(productId);
        verifyOwnership(inventory, vendorId);
        return inventoryMapper.toMovementResponseList(
                stockMovementRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    // ---------------------------------------------------------------
    // Writes
    // ---------------------------------------------------------------

    @Override
    public InventoryResponse initializeInventory(UUID productId, Long vendorId,
            int initialStock, int reorderThreshold) {

        if (inventoryRepository.findByProduct_Id(productId).isPresent()) {
            throw new DuplicateResourceException("Inventory already exists for this product.");
        }
        if (initialStock < 0) {
            throw new BadRequestException("Initial stock cannot be negative.");
        }

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found."));

        Inventory inventory = Inventory.builder()
                .product(product)
                .vendorId(vendorId)
                .availableStock(initialStock)
                .reservedStock(0)
                .reorderThreshold(reorderThreshold)
                .build();
        inventory = inventoryRepository.save(inventory);

        if (initialStock > 0) {
            logMovement(inventory, MovementType.RESTOCK, initialStock,
                    0, initialStock, null, null, "Initial stock on product approval");
        }

        return inventoryMapper.toResponse(inventory);
    }

    @Override
    public InventoryResponse restock(UUID productId, int quantity, UUID performedBy, String note) {
        return doRestock(findInventoryOrThrow(productId), quantity, performedBy, note);
    }

    @Override
    public InventoryResponse restock(UUID productId, int quantity, UUID performedBy, String note, Long vendorId) {
        Inventory inventory = findInventoryOrThrow(productId);
        verifyOwnership(inventory, vendorId);
        return doRestock(inventory, quantity, performedBy, note);
    }

    @Override
    public InventoryResponse adjustStock(UUID productId, int delta, UUID performedBy, String note) {
        return doAdjust(findInventoryOrThrow(productId), delta, performedBy, note);
    }

    @Override
    public InventoryResponse adjustStock(UUID productId, int delta, UUID performedBy, String note, Long vendorId) {
        Inventory inventory = findInventoryOrThrow(productId);
        verifyOwnership(inventory, vendorId);
        return doAdjust(inventory, delta, performedBy, note);
    }

    @Override
    public void reserveStock(UUID productId, int quantity, UUID orderId, UUID performedBy) {
        if (quantity <= 0) {
            throw new BadRequestException("Reserve quantity must be positive.");
        }

        Inventory inventory = findInventoryOrThrow(productId);
        if (inventory.getAvailableStock() < quantity) {
            throw new ConflictException(
                    "Insufficient stock for product " + productId + ". Available: "
                            + inventory.getAvailableStock() + ", requested: " + quantity);
        }

        int previousAvailable = inventory.getAvailableStock();
        inventory.setAvailableStock(previousAvailable - quantity);
        inventory.setReservedStock(inventory.getReservedStock() + quantity);
        saveWithLockCheck(inventory);

        logMovement(inventory, MovementType.RESERVATION, -quantity,
                previousAvailable, inventory.getAvailableStock(), orderId, performedBy,
                "Reserved at checkout");
    }

    @Override
    public void commitStock(UUID orderId, UUID performedBy) {
        if (stockMovementRepository.existsByReferenceOrderIdAndMovementType(orderId, MovementType.SALE)) {
            return; // already committed — safe no-op for a retried webhook
        }

        List<StockMovement> reservations = stockMovementRepository
                .findByReferenceOrderIdAndMovementType(orderId, MovementType.RESERVATION);
        if (reservations.isEmpty()) {
            throw new ResourceNotFoundException("No stock reservations found for order " + orderId);
        }

        for (StockMovement reservation : reservations) {
            Inventory inventory = reservation.getInventory();
            int qty = -reservation.getChangeQty();

            int previousReserved = inventory.getReservedStock();
            inventory.setReservedStock(previousReserved - qty);
            saveWithLockCheck(inventory);

            logMovement(inventory, MovementType.SALE, -qty,
                    previousReserved, inventory.getReservedStock(), orderId, performedBy,
                    "Committed on payment success");
        }
    }

    @Override
    public void releaseStock(UUID orderId, UUID performedBy) {
        if (stockMovementRepository.existsByReferenceOrderIdAndMovementType(orderId, MovementType.RELEASE)) {
            return; // already released — safe no-op for a retried webhook
        }

        List<StockMovement> reservations = stockMovementRepository
                .findByReferenceOrderIdAndMovementType(orderId, MovementType.RESERVATION);
        if (reservations.isEmpty()) {
            throw new ResourceNotFoundException("No stock reservations found for order " + orderId);
        }

        for (StockMovement reservation : reservations) {
            Inventory inventory = reservation.getInventory();
            int qty = -reservation.getChangeQty();

            int previousAvailable = inventory.getAvailableStock();
            inventory.setReservedStock(inventory.getReservedStock() - qty);
            inventory.setAvailableStock(previousAvailable + qty);
            saveWithLockCheck(inventory);

            logMovement(inventory, MovementType.RELEASE, qty,
                    previousAvailable, inventory.getAvailableStock(), orderId, performedBy,
                    "Released on payment failure/cancellation");
        }
    }

    // ---------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------

    private InventoryResponse doRestock(Inventory inventory, int quantity, UUID performedBy, String note) {
        if (quantity <= 0) {
            throw new BadRequestException("Restock quantity must be positive.");
        }
        int previous = inventory.getAvailableStock();
        inventory.setAvailableStock(previous + quantity);
        saveWithLockCheck(inventory);

        logMovement(inventory, MovementType.RESTOCK, quantity,
                previous, inventory.getAvailableStock(), null, performedBy, note);

        return inventoryMapper.toResponse(inventory);
    }

    private InventoryResponse doAdjust(Inventory inventory, int delta, UUID performedBy, String note) {
        if (delta == 0) {
            throw new BadRequestException("Adjustment delta must be non-zero.");
        }
        int previous = inventory.getAvailableStock();
        int updated = previous + delta;
        if (updated < 0) {
            throw new BadRequestException("Adjustment would result in negative available stock.");
        }
        inventory.setAvailableStock(updated);
        saveWithLockCheck(inventory);

        logMovement(inventory, MovementType.ADJUSTMENT, delta,
                previous, updated, null, performedBy, note);

        return inventoryMapper.toResponse(inventory);
    }

    private Inventory findInventoryOrThrow(UUID productId) {
        return inventoryRepository.findByProduct_Id(productId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Inventory not found for product " + productId));
    }

    private void verifyOwnership(Inventory inventory, Long vendorId) {
        if (!inventory.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("This inventory record does not belong to the current vendor.");
        }
    }

    /**
     * Inventory rows inherit @Version from BaseEntity, so concurrent
     * checkouts/restocks on the same product collide here rather than
     * silently overselling. Surface it as a 409 the caller can retry.
     */
    private void saveWithLockCheck(Inventory inventory) {
        try {
            inventoryRepository.save(inventory);
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new ConflictException(
                    "Stock for this product was updated concurrently. Please retry.");
        }
    }

    private void logMovement(Inventory inventory, MovementType type, int changeQty,
            int previousStock, int newStock, UUID orderId,
            UUID performedBy, String note) {
        StockMovement movement = StockMovement.builder()
                .inventory(inventory)
                .productId(inventory.getProduct().getId())
                .vendorId(inventory.getVendorId())
                .movementType(type)
                .changeQty(changeQty)
                .previousStock(previousStock)
                .newStock(newStock)
                .referenceOrderId(orderId)
                .performedBy(performedBy)
                .note(note)
                .build();
        stockMovementRepository.save(movement);
    }
}