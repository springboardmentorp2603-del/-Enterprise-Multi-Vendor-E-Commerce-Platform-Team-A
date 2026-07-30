package com.shopstack.modules.coupon.dto.responses;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CouponValidationResponse {

    private boolean valid;
    private UUID couponId;
    private String code;
    private BigDecimal discountAmount;
    private BigDecimal finalTotal;
}