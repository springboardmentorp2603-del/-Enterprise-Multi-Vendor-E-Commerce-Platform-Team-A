package com.shopstack.modules.inventory.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class ReserveStockRequest {

    @NotNull
    private UUID productId;

    @NotNull
    @Min(1)
    private Integer quantity;

    // The pending order this reservation belongs to; used to look the
    // reservation back up in commitStock/releaseStock.
    @NotNull
    private UUID orderId;
}