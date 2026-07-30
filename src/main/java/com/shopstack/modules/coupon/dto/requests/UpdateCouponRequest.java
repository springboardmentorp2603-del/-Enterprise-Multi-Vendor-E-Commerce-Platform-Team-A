package com.shopstack.modules.coupon.dto.requests;

import com.shopstack.common.enums.DiscountType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class UpdateCouponRequest {

    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimitTotal;
    private Integer usageLimitPerUser;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Boolean active;
}