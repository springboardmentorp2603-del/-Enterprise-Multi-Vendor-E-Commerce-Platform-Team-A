package com.shopstack.modules.warehouse.dto.requests;

import jakarta.validation.constraints.NotNull;
import lombok.*;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AllocateInventoryRequest {

    @NotNull(message = "Product ID is required")
    private UUID productId;

    @NotNull(message = "Warehouse ID is required")
    private UUID warehouseId;

    private Long vendorId; // Optional, defaults to current authenticated vendor
}
