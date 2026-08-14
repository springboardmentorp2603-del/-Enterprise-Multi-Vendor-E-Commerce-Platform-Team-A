package com.shopstack.modules.warehouse.mapper;

import com.shopstack.common.config.MapStructConfig;
import com.shopstack.modules.warehouse.dto.requests.WarehouseRequest;
import com.shopstack.modules.warehouse.dto.responses.WarehouseResponse;
import com.shopstack.modules.warehouse.dto.responses.WarehouseFulfillmentResponse;
import com.shopstack.modules.warehouse.entity.Warehouse;
import com.shopstack.modules.warehouse.entity.WarehouseOrderFulfillment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(config = MapStructConfig.class)
public interface WarehouseMapper {

    @Mapping(target = "occupancy", ignore = true)
    WarehouseResponse toResponse(Warehouse warehouse);

    List<WarehouseResponse> toResponseList(List<Warehouse> warehouses);

    WarehouseFulfillmentResponse toResponse(WarehouseOrderFulfillment fulfillment);

    List<WarehouseFulfillmentResponse> toFulfillmentResponseList(List<WarehouseOrderFulfillment> fulfillments);

    Warehouse toEntity(WarehouseRequest request);
}
