package com.shopstack.modules.product.dto.responses;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class CategoryResponse {

    private UUID id;
    private String categoryName;
    private String description;
    private Boolean active;
}