package com.shopstack.modules.inventory.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryResponse {
    private UUID productId;
    private Long vendorId;
    private Integer availableStock;
    private Integer reservedStock;
    private Integer reorderThreshold;
    private Boolean lowStock;
}