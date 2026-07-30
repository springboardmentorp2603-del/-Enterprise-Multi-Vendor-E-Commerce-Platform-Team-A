package com.shopstack.modules.pricing.dto.requests;

import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateDiscountRequest {

    // Null fields are left unchanged by the service (partial update).
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal value;

    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Boolean active;
}