package com.shopstack.modules.pricing.dto.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
public class BulkPriceUpdateRequest {

    @NotEmpty
    @Valid
    private List<Item> items;

    @Getter
    @Setter
    public static class Item {

        @NotNull
        private UUID productId;

        @NotNull
        @DecimalMin(value = "0.0", inclusive = true)
        private BigDecimal price;
    }
}