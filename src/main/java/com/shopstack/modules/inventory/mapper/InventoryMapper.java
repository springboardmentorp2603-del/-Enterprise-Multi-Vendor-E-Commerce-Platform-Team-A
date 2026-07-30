package com.shopstack.modules.inventory.mapper;

import com.shopstack.common.config.MapStructConfig;
import com.shopstack.modules.inventory.dto.responses.InventoryResponse;
import com.shopstack.modules.inventory.dto.responses.StockMovementResponse;
import com.shopstack.modules.inventory.entity.Inventory;
import com.shopstack.modules.inventory.entity.StockMovement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface InventoryMapper {

    @Mapping(source = "product.id", target = "productId")
    // "lowStock" maps automatically from Inventory#isLowStock() by
    // MapStruct's bean-property naming convention — no explicit
    // @Mapping needed since it's a derived/transient getter, not a column.
    InventoryResponse toResponse(Inventory inventory);

    List<InventoryResponse> toResponseList(List<Inventory> inventories);

    StockMovementResponse toResponse(StockMovement movement);

    List<StockMovementResponse> toMovementResponseList(List<StockMovement> movements);
}