package com.shopstack.modules.coupon.dto.requests;

import com.shopstack.common.enums.DiscountType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class CreateCouponRequest {

    @NotBlank(message = "Coupon code is required")
    private String code;

    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    @Positive(message = "Discount value must be greater than zero")
    private BigDecimal discountValue;

    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimitTotal;
    private Integer usageLimitPerUser;

    @NotNull(message = "Valid-from date is required")
    private LocalDateTime validFrom;

    @NotNull(message = "Valid-to date is required")
    private LocalDateTime validTo;

    private Long vendorId; // null = platform-wide coupon
}