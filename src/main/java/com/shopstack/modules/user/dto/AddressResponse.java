package com.shopstack.modules.user.dto;

import com.shopstack.common.enums.AddressType;
import lombok.Builder;
import lombok.Getter;
import java.util.UUID;

@Getter
@Builder
public class AddressResponse {
    private UUID id;
    private AddressType type;
    private String street;
    private String landmark;
    private String city;
    private String state;
    private String country;
    private String postalCode;
}