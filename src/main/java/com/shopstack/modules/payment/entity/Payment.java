package com.shopstack.modules.payment.entity;

import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "payments")
@Getter
@Setter
@NoArgsConstructor
public class Payment extends BaseEntity {

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String currency = "INR";

    @Column(nullable = false)
    private String status = "PENDING";

    private String razorpayOrderId;

    private String razorpayPaymentId;

    private String paymentMethod;
}