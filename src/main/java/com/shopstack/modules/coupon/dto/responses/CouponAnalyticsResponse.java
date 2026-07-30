package com.shopstack.modules.coupon.dto.responses;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Builder
public class CouponAnalyticsResponse {

    private UUID couponId;
    private String code;
    private long totalTimesUsed;
    private Integer usageLimitTotal;
    private BigDecimal totalDiscountGiven;
}