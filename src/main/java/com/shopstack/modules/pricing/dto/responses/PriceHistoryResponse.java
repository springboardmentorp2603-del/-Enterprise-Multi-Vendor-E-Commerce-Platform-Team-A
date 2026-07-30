package com.shopstack.modules.pricing.dto.responses;

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
public class PriceHistoryResponse {
    private UUID id;
    private UUID productId;
    private BigDecimal oldPrice;
    private BigDecimal newPrice;
    private UUID changedBy;
    private String note;
    private LocalDateTime createdAt;
}