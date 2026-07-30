package com.shopstack.modules.vendor.dto.requests;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorRegistrationRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    @NotBlank(message = "Business name is required")
    @Size(max = 150)
    private String businessName;

    @NotBlank(message = "Business email is required")
    @Email(message = "Business email must be valid")
    @Size(max = 150)
    private String businessEmail;

    @NotBlank(message = "Business phone is required")
    @Pattern(regexp = "^[+]?[0-9]{7,20}$", message = "Invalid phone number format")
    private String businessPhone;

    @Size(max = 30)
    private String gstNumber;

    @Size(max = 20)
    private String panNumber;

    @Size(max = 50)
    private String businessType;

    private String logoUrl;

    private String description;

    @Valid
    @NotEmpty(message = "At least one address is required")
    private List<VendorAddressRequest> addresses;

    @Valid
    private List<VendorBankDetailsRequest> bankDetails;

    @Valid
    private List<VendorDocumentUploadRequest> documents;
}
