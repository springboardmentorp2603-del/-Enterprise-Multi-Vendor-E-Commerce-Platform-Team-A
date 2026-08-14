package com.shopstack.modules.product.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryCommissionRateDto {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private BigDecimal commissionRate;
    private Boolean active;
    private LocalDateTime effectiveFrom;
    private LocalDateTime effectiveTo;
}
