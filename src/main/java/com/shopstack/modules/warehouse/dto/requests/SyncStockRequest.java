package com.shopstack.modules.warehouse.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SyncStockRequest {

    @NotNull(message = "New quantity is required")
    @Min(value = 0, message = "New quantity cannot be negative")
    private Integer newQuantity;

    private String note;
}
