package com.shopstack.modules.order.entity;

import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShippingAddress {
    private String street;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}