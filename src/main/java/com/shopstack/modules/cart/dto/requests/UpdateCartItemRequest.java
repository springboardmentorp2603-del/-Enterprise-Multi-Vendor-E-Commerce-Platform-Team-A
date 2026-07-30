package com.shopstack.modules.cart.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateCartItemRequest {
    private Integer quantity;
}