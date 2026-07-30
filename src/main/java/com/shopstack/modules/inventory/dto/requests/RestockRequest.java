package com.shopstack.modules.inventory.dto.requests;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RestockRequest {

    @NotNull
    @Min(1)
    private Integer quantity;

    private String note;
}