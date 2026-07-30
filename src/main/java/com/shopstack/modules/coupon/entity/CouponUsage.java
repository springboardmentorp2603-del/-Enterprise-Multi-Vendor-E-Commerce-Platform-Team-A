package com.shopstack.modules.coupon.entity;

import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "coupon_usages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponUsage extends BaseEntity {

    @Column(name = "coupon_id", nullable = false)
    private UUID couponId;

    @Column(name = "user_id")
    private UUID userId; // nullable - guest checkouts have no user

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "discount_applied", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountApplied;
}