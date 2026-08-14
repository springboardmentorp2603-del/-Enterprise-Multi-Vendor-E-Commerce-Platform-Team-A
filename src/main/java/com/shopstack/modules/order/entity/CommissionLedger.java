package com.shopstack.modules.order.entity;

import com.shopstack.common.audit.BaseEntity;
import com.shopstack.modules.order.enums.LedgerStatus;
import com.shopstack.modules.order.enums.LedgerTransactionType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(
    name = "commission_ledger",
    indexes = {
        @Index(name = "idx_ledger_vendor_id", columnList = "vendor_id"),
        @Index(name = "idx_ledger_order_id", columnList = "order_id"),
        @Index(name = "idx_ledger_order_item_id", columnList = "order_item_id"),
        @Index(name = "idx_ledger_created_at", columnList = "created_at"),
        @Index(name = "idx_ledger_transaction_type", columnList = "transaction_type"),
        @Index(name = "idx_ledger_status", columnList = "status")
    }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommissionLedger extends BaseEntity {

    @Column(name = "vendor_id", nullable = false)
    private Long vendorId;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "order_item_id", nullable = false)
    private UUID orderItemId;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @Column(name = "commission_rate", nullable = false, precision = 5, scale = 2)
    private BigDecimal commissionRate;

    @Column(name = "gross_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "commission_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal commissionAmount;

    @Column(name = "vendor_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal vendorAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private LedgerTransactionType transactionType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private LedgerStatus status;

    @Column(name = "reference_ledger_id")
    private UUID referenceLedgerId;

    @Column(name = "description", length = 255)
    private String description;
}
