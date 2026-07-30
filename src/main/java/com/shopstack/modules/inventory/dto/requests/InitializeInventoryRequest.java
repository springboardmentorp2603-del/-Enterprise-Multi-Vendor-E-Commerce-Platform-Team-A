package com.shopstack.modules.inventory.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class InitializeInventoryRequest {

    @NotNull
    private UUID productId;

    // Not required from a VENDOR caller: derived from their logged-in
    // vendor profile. ADMIN callers may supply it explicitly.
    private Long vendorId;

    @NotNull
    @Min(0)
    private Integer initialStock;

    @Min(0)
    private Integer reorderThreshold = 10;
}