package com.shopstack.modules.order.entity;

import com.shopstack.common.audit.BaseEntity;
import com.shopstack.modules.product.entity.Product;
import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class OrderItem extends BaseEntity {

    @JsonBackReference
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @JsonIgnore
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // ← THESE ARE THE FIELDS THAT GET SERIALIZED TO JSON
    @Column(name = "product_name")
    private String productName;

    @Column(name = "product_id_copy")
    private String productId;

    @Column(name = "product_image")
    private String productImage;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Double price;
}