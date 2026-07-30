package com.shopstack.modules.product.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCategoryRequest {

    @NotBlank
    @Size(max = 100)
    private String categoryName;

    @Size(max = 500)
    private String description;

    private Boolean active;
}