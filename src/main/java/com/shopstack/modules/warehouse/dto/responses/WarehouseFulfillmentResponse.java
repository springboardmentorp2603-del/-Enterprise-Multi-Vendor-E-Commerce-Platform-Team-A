package com.shopstack.modules.warehouse.dto.responses;

import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WarehouseFulfillmentResponse {
    private UUID id;
    private UUID orderId;
    private UUID warehouseId;
    private String status;
    private UUID assignedStaffId;
    private String trackingNumber;
    private String carrier;
    private String notes;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
