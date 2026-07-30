package com.shopstack.modules.pricing.dto.requests;

import com.shopstack.common.enums.DiscountType;
import com.shopstack.common.enums.ScopeType;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class CreateDiscountRequest {

    @NotNull
    private ScopeType scopeType;

    // Exactly one of these must be set, matching scopeType.
    private UUID productId;
    private UUID categoryId;

    @NotNull
    private DiscountType discountType;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal value;

    @NotNull
    private LocalDateTime startDate;

    @NotNull
    @Future
    private LocalDateTime endDate;

    @AssertTrue(message = "Exactly one of productId or categoryId must be set, matching scopeType.")
    private boolean isScopeConsistent() {
        if (scopeType == null) return true; // let @NotNull report this one
        boolean productScoped = scopeType == ScopeType.PRODUCT;
        return productScoped
                ? (productId != null && categoryId == null)
                : (categoryId != null && productId == null);
    }

    @AssertTrue(message = "Percentage discount value cannot exceed 100.")
    private boolean isPercentageWithinRange() {
        if (discountType == null || value == null) return true;
        return discountType != DiscountType.PERCENTAGE || value.compareTo(BigDecimal.valueOf(100)) <= 0;
    }

    @AssertTrue(message = "endDate must be after startDate.")
    private boolean isDateRangeValid() {
        if (startDate == null || endDate == null) return true;
        return endDate.isAfter(startDate);
    }
}