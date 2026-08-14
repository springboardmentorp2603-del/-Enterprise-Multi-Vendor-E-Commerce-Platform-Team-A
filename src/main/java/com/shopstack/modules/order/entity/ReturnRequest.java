package com.shopstack.modules.order.entity;

import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "return_requests")
@Getter
@Setter
@NoArgsConstructor
public class ReturnRequest extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String reason;

    @Column(nullable = false)
    private String refundType;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private String status; // PENDING, APPROVED, REJECTED, PROCESSED

}
