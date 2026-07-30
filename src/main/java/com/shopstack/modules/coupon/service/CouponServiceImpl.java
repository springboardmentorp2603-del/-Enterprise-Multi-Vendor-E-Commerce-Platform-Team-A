package com.shopstack.modules.coupon.service;

import com.shopstack.common.enums.DiscountType;
import com.shopstack.common.exception.BadRequestException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.coupon.dto.requests.CreateCouponRequest;
import com.shopstack.modules.coupon.dto.requests.UpdateCouponRequest;
import com.shopstack.modules.coupon.dto.responses.CouponAnalyticsResponse;
import com.shopstack.modules.coupon.dto.responses.CouponResponse;
import com.shopstack.modules.coupon.dto.responses.CouponValidationResponse;
import com.shopstack.modules.coupon.entity.Coupon;
import com.shopstack.modules.coupon.entity.CouponUsage;
import com.shopstack.modules.coupon.repository.CouponRepository;
import com.shopstack.modules.coupon.repository.CouponUsageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CouponServiceImpl implements CouponService {

    private final CouponRepository couponRepository;
    private final CouponUsageRepository couponUsageRepository;

    @Override
    @Transactional
    public CouponResponse createCoupon(CreateCouponRequest request) {
        if (couponRepository.existsByCodeIgnoreCase(request.getCode())) {
            throw new BadRequestException("A coupon with code '" + request.getCode() + "' already exists");
        }
        if (!request.getValidTo().isAfter(request.getValidFrom())) {
            throw new BadRequestException("Valid-to date must be after valid-from date");
        }

        Coupon coupon = Coupon.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
                .discountType(request.getDiscountType())
                .discountValue(request.getDiscountValue())
                .minOrderAmount(request.getMinOrderAmount() != null ? request.getMinOrderAmount() : BigDecimal.ZERO)
                .maxDiscountAmount(request.getMaxDiscountAmount())
                .usageLimitTotal(request.getUsageLimitTotal())
                .usageLimitPerUser(request.getUsageLimitPerUser())
                .validFrom(request.getValidFrom())
                .validTo(request.getValidTo())
                .vendorId(request.getVendorId())
                .active(true)
                .build();

        return toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional(readOnly = true)
    public List<CouponResponse> listCoupons() {
        return couponRepository.findAllByOrderByCreatedAtDesc()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CouponResponse getCoupon(UUID id) {
        return toResponse(findCouponOrThrow(id));
    }

    @Override
    @Transactional
    public CouponResponse updateCoupon(UUID id, UpdateCouponRequest request) {
        Coupon coupon = findCouponOrThrow(id);

        if (request.getDescription() != null) coupon.setDescription(request.getDescription());
        if (request.getDiscountType() != null) coupon.setDiscountType(request.getDiscountType());
        if (request.getDiscountValue() != null) coupon.setDiscountValue(request.getDiscountValue());
        if (request.getMinOrderAmount() != null) coupon.setMinOrderAmount(request.getMinOrderAmount());
        if (request.getMaxDiscountAmount() != null) coupon.setMaxDiscountAmount(request.getMaxDiscountAmount());
        if (request.getUsageLimitTotal() != null) coupon.setUsageLimitTotal(request.getUsageLimitTotal());
        if (request.getUsageLimitPerUser() != null) coupon.setUsageLimitPerUser(request.getUsageLimitPerUser());
        if (request.getValidFrom() != null) coupon.setValidFrom(request.getValidFrom());
        if (request.getValidTo() != null) coupon.setValidTo(request.getValidTo());
        if (request.getActive() != null) coupon.setActive(request.getActive());

        return toResponse(couponRepository.save(coupon));
    }

    @Override
    @Transactional
    public void deactivateCoupon(UUID id) {
        Coupon coupon = findCouponOrThrow(id);
        coupon.setActive(false);
        couponRepository.save(coupon);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponValidationResponse validateCoupon(String code, UUID userId, BigDecimal cartTotal) {
        if (code == null || code.isBlank()) {
            throw new BadRequestException("Coupon code is required");
        }

        Coupon coupon = couponRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new BadRequestException("Invalid coupon code"));

        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new BadRequestException("This coupon is no longer available");
        }

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(coupon.getValidFrom())) {
            throw new BadRequestException("This coupon is not active yet");
        }
        if (now.isAfter(coupon.getValidTo())) {
            throw new BadRequestException("This coupon has expired");
        }

        if (coupon.getMinOrderAmount() != null && cartTotal.compareTo(coupon.getMinOrderAmount()) < 0) {
            throw new BadRequestException(
                    "Minimum order amount of " + coupon.getMinOrderAmount() + " required for this coupon");
        }

        if (coupon.getUsageLimitTotal() != null) {
            long totalUses = couponUsageRepository.countByCouponId(coupon.getId());
            if (totalUses >= coupon.getUsageLimitTotal()) {
                throw new BadRequestException("This coupon has reached its usage limit");
            }
        }

        if (coupon.getUsageLimitPerUser() != null && userId != null) {
            long userUses = couponUsageRepository.countByCouponIdAndUserId(coupon.getId(), userId);
            if (userUses >= coupon.getUsageLimitPerUser()) {
                throw new BadRequestException("You have already used this coupon the maximum number of times");
            }
        }

        BigDecimal discount;
        if (coupon.getDiscountType() == DiscountType.PERCENTAGE) {
            discount = cartTotal.multiply(coupon.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
            if (coupon.getMaxDiscountAmount() != null && discount.compareTo(coupon.getMaxDiscountAmount()) > 0) {
                discount = coupon.getMaxDiscountAmount();
            }
        } else {
            discount = coupon.getDiscountValue();
        }

        if (discount.compareTo(cartTotal) > 0) {
            discount = cartTotal;
        }

        BigDecimal finalTotal = cartTotal.subtract(discount);

        return CouponValidationResponse.builder()
                .valid(true)
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .discountAmount(discount)
                .finalTotal(finalTotal)
                .build();
    }

    @Override
    @Transactional
    public void recordCouponUsage(UUID couponId, UUID userId, UUID orderId, BigDecimal discountApplied) {
        CouponUsage usage = CouponUsage.builder()
                .couponId(couponId)
                .userId(userId)
                .orderId(orderId)
                .discountApplied(discountApplied)
                .build();
        couponUsageRepository.save(usage);
    }

    @Override
    @Transactional(readOnly = true)
    public CouponAnalyticsResponse getAnalytics(UUID id) {
        Coupon coupon = findCouponOrThrow(id);
        long totalUses = couponUsageRepository.countByCouponId(id);
        BigDecimal totalDiscount = couponUsageRepository.sumDiscountByCouponId(id);

        return CouponAnalyticsResponse.builder()
                .couponId(coupon.getId())
                .code(coupon.getCode())
                .totalTimesUsed(totalUses)
                .usageLimitTotal(coupon.getUsageLimitTotal())
                .totalDiscountGiven(totalDiscount)
                .build();
    }

    private Coupon findCouponOrThrow(UUID id) {
        return couponRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Coupon not found"));
    }

    private CouponResponse toResponse(Coupon coupon) {
        long totalUses = couponUsageRepository.countByCouponId(coupon.getId());
        return CouponResponse.builder()
                .id(coupon.getId())
                .code(coupon.getCode())
                .description(coupon.getDescription())
                .discountType(coupon.getDiscountType())
                .discountValue(coupon.getDiscountValue())
                .minOrderAmount(coupon.getMinOrderAmount())
                .maxDiscountAmount(coupon.getMaxDiscountAmount())
                .usageLimitTotal(coupon.getUsageLimitTotal())
                .usageLimitPerUser(coupon.getUsageLimitPerUser())
                .validFrom(coupon.getValidFrom())
                .validTo(coupon.getValidTo())
                .vendorId(coupon.getVendorId())
                .active(coupon.getActive())
                .totalTimesUsed(totalUses)
                .build();
    }
}