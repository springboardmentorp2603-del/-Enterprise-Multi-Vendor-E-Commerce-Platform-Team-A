package com.shopstack.modules.inventory.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdjustStockRequest {

    // Positive to add stock, negative to remove. Zero is rejected by the service.
    @NotNull
    private Integer delta;

    private String note;
}