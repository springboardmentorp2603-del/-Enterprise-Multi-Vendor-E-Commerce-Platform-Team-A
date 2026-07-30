package com.shopstack.modules.cart.dto.requests;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class AddCartItemRequest {
    private UUID productId;
    private Integer quantity;
}