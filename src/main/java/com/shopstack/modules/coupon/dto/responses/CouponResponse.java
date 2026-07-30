package com.shopstack.modules.coupon.dto.responses;

import com.shopstack.common.enums.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class CouponResponse {

    private UUID id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private Integer usageLimitTotal;
    private Integer usageLimitPerUser;
    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private Long vendorId;
    private Boolean active;
    private long totalTimesUsed;
}