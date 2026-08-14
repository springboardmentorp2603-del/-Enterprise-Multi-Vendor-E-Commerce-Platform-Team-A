package com.shopstack.modules.warehouse.dto.responses;

import lombok.*;
import java.util.Map;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseAnalyticsResponse {
    private UUID warehouseId;
    private String warehouseName;
    private Integer totalCapacity;
    private Integer currentOccupancy;
    private Double occupancyRate;
    private Long activeFulfillmentsCount;
    private Map<String, Long> fulfillmentsByStatus;
    private Long lowStockItemsCount;
}
