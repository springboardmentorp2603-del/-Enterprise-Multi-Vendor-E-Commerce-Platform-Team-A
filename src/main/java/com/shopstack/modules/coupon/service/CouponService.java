package com.shopstack.modules.coupon.service;

import com.shopstack.modules.coupon.dto.requests.CreateCouponRequest;
import com.shopstack.modules.coupon.dto.requests.UpdateCouponRequest;
import com.shopstack.modules.coupon.dto.responses.CouponAnalyticsResponse;
import com.shopstack.modules.coupon.dto.responses.CouponResponse;
import com.shopstack.modules.coupon.dto.responses.CouponValidationResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface CouponService {

    CouponResponse createCoupon(CreateCouponRequest request);

    List<CouponResponse> listCoupons();

    CouponResponse getCoupon(UUID id);

    CouponResponse updateCoupon(UUID id, UpdateCouponRequest request);

    void deactivateCoupon(UUID id);

    CouponValidationResponse validateCoupon(String code, UUID userId, BigDecimal cartTotal);

    void recordCouponUsage(UUID couponId, UUID userId, UUID orderId, BigDecimal discountApplied);

    CouponAnalyticsResponse getAnalytics(UUID id);
}