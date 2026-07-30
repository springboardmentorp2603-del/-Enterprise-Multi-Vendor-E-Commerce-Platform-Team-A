package com.shopstack.modules.product.entity;

import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "customer_name", nullable = false, length = 100)
    private String customerName;

    @Column(name = "rating", nullable = false)
    private Integer rating;

    @Column(name = "review", columnDefinition = "TEXT")
    private String review;
}