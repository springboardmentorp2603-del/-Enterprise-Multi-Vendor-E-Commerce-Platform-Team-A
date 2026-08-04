package com.shopstack.modules.coupon.dto.responses;

import com.shopstack.common.enums.DiscountType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class PublicCouponResponse {

    private UUID id;
    private String code;
    private String description;
    private DiscountType discountType;
    private BigDecimal discountValue;
    private BigDecimal minOrderAmount;
    private BigDecimal maxDiscountAmount;
    private LocalDateTime validTo;
    private Long vendorId; // null = platform-wide

    // Only populated when a cartTotal was passed in — tells the customer if THEY can use it right now
    private Boolean eligibleForCart;
}