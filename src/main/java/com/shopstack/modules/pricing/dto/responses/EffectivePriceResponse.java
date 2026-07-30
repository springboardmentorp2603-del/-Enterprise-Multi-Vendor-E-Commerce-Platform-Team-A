package com.shopstack.modules.pricing.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EffectivePriceResponse {
    private UUID productId;
    private BigDecimal basePrice;
    private BigDecimal discountAmount;
    private BigDecimal effectivePrice;

    // Null if no discount applied. Lets Cart/Checkout show "20% off" etc.
    // and lets Order snapshot exactly which discount was used.
    private UUID appliedDiscountId;
}