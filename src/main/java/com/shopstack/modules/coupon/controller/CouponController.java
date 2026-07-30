package com.shopstack.modules.coupon.controller;

import com.shopstack.common.response.ApiResponse;
import com.shopstack.common.response.ApiResponseBuilder;
import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.coupon.dto.requests.CreateCouponRequest;
import com.shopstack.modules.coupon.dto.requests.UpdateCouponRequest;
import com.shopstack.modules.coupon.dto.requests.ValidateCouponRequest;
import com.shopstack.modules.coupon.dto.responses.CouponAnalyticsResponse;
import com.shopstack.modules.coupon.dto.responses.CouponResponse;
import com.shopstack.modules.coupon.dto.responses.CouponValidationResponse;
import com.shopstack.modules.coupon.service.CouponService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
public class CouponController {

    private final CouponService couponService;
    private final SecurityUtil securityUtil;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    public ApiResponse<CouponResponse> create(@Valid @RequestBody CreateCouponRequest request) {
        return ApiResponseBuilder.success("Coupon created", couponService.createCoupon(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    public ApiResponse<List<CouponResponse>> list() {
        return ApiResponseBuilder.success("Coupons fetched", couponService.listCoupons());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    public ApiResponse<CouponResponse> getOne(@PathVariable UUID id) {
        return ApiResponseBuilder.success("Coupon fetched", couponService.getCoupon(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    public ApiResponse<CouponResponse> update(@PathVariable UUID id, @RequestBody UpdateCouponRequest request) {
        return ApiResponseBuilder.success("Coupon updated", couponService.updateCoupon(id, request));
    }

    @PostMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    public ApiResponse<Void> deactivate(@PathVariable UUID id) {
        couponService.deactivateCoupon(id);
        return ApiResponseBuilder.success("Coupon deactivated");
    }

    @PostMapping("/validate")
    public ApiResponse<CouponValidationResponse> validate(@Valid @RequestBody ValidateCouponRequest request) {
        UUID userId = resolveCurrentUserIdOrNull();
        return ApiResponseBuilder.success(
                "Coupon is valid",
                couponService.validateCoupon(request.getCode(), userId, request.getCartTotal()));
    }

    @GetMapping("/{id}/analytics")
    @PreAuthorize("hasAnyRole('ADMIN','VENDOR')")
    public ApiResponse<CouponAnalyticsResponse> analytics(@PathVariable UUID id) {
        return ApiResponseBuilder.success("Coupon analytics fetched", couponService.getAnalytics(id));
    }

    private UUID resolveCurrentUserIdOrNull() {
        try {
            return securityUtil.getCurrentUser().getId();
        } catch (RuntimeException ex) {
            return null; // guest checkout - coupon validation still allowed
        }
    }
}