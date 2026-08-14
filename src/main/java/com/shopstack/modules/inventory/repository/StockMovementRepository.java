package com.shopstack.modules.inventory.repository;

import com.shopstack.common.enums.MovementType;
import com.shopstack.modules.inventory.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface StockMovementRepository extends JpaRepository<StockMovement, UUID> {

    // Audit trail for a single product (Reports & Export / vendor stock history screen)
    List<StockMovement> findByProductIdOrderByCreatedAtDesc(UUID productId);

    // Used by commitStock/releaseStock to find the reservations made at checkout for an order
    List<StockMovement> findByReferenceOrderIdAndMovementType(UUID referenceOrderId, MovementType movementType);

    // Idempotency guard: has this order already been committed/released?
    // Payment webhooks can be delivered more than once, so this must be
    // checked before applying a SALE or RELEASE movement.
    boolean existsByReferenceOrderIdAndMovementType(UUID referenceOrderId, MovementType movementType);

     @Query("select sm from StockMovement sm where sm.inventory.warehouseId = :warehouseId")
    List<StockMovement> findByWarehouseId(@Param("warehouseId") UUID warehouseId);
}