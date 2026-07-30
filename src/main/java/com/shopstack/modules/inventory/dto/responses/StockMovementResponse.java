package com.shopstack.modules.inventory.dto.responses;

import com.shopstack.common.enums.MovementType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockMovementResponse{
    private UUID id;
    private UUID productId;
    private MovementType movementType;
    private Integer changeQty;
    private Integer previousStock;
    private Integer newStock;
    private UUID referenceOrderId;
    private UUID performedBy;
    private String note;
    private LocalDateTime createdAt;
}