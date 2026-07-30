package com.shopstack.modules.vendor.dto.responses;

import com.shopstack.common.enums.AddressType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorAddressResponse {

    private Long addressId;
    private AddressType addressType;
    private String addressLine1;
    private String addressLine2;
    private String city;
    private String state;
    private String country;
    private String postalCode;
    private Boolean isDefault;
}