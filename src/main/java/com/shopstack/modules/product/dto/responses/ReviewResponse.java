package com.shopstack.modules.product.dto.responses;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class ReviewResponse {

    private UUID id;
    private String customerName;
    private Integer rating;
    private String review;
}