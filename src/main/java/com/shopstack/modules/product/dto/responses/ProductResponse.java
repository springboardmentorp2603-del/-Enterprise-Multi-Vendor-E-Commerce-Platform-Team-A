package com.shopstack.modules.product.dto.responses;

import com.shopstack.common.enums.ProductApprovalStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
public class ProductResponse {

    private UUID id;
    private String productName;
    private String brand;
    private String description;
    private String imageUrl;
    private String features;
    private BigDecimal price;
    private Integer stockQuantity;
    private BigDecimal discountPercentage;
    private Long vendorId;
    private UUID categoryId;
    private String categoryName;
    private Boolean featured;
    private Boolean active;
    private ProductApprovalStatus approvalStatus;
    private String rejectionReason;
    private LocalDateTime reviewedAt;
}