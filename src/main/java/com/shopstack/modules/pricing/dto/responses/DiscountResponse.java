package com.shopstack.modules.pricing.dto.responses;

import com.shopstack.common.enums.DiscountType;
import com.shopstack.common.enums.ScopeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiscountResponse {
    private UUID id;
    private Long vendorId;
    private ScopeType scopeType;
    private UUID productId;
    private UUID categoryId;
    private DiscountType discountType;
    private BigDecimal value;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
    private Boolean currentlyActive;
}