package com.shopstack.modules.vendor.dto.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorUpdateRequest {

    @Size(max = 150)
    private String businessName;

    @Email(message = "Business email must be valid")
    @Size(max = 150)
    private String businessEmail;

    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Invalid phone number format")
    private String businessPhone;

    @Size(max = 50)
    private String businessType;

    private String logoUrl;

    private String description;
}