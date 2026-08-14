package com.shopstack.modules.inventory.repository;

import com.shopstack.modules.inventory.entity.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InventoryRepository extends JpaRepository<Inventory, UUID> {

    Optional<Inventory> findByProduct_Id(UUID productId);

    List<Inventory> findByVendorId(Long vendorId);

     List<Inventory> findByWarehouseId(UUID warehouseId);

    // Derived query methods can't compare two columns on the same row
    // (available_stock <= reorder_threshold), so this needs JPQL.
    @Query("select i from Inventory i where i.vendorId = :vendorId " +
           "and i.availableStock <= i.reorderThreshold")
    List<Inventory> findLowStockByVendor(@Param("vendorId") Long vendorId);

    @Query("select i from Inventory i where i.availableStock <= i.reorderThreshold")
    List<Inventory> findAllLowStock();
}