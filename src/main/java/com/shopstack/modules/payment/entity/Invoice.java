package com.shopstack.modules.payment.entity;

import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "invoices")
@Getter
@Setter
@NoArgsConstructor
public class Invoice extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    @Column(nullable = false)
    private UUID orderId;

    @Column(nullable = false)
    private UUID paymentId;

    @Column(nullable = false)
    private Double amount;

    private Double gstAmount;

    private String pdfPath;
}