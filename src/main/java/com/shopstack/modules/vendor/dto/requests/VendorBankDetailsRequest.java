package com.shopstack.modules.vendor.dto.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorBankDetailsRequest {

    @NotBlank(message = "Account holder name is required")
    @Size(max = 150)
    private String accountHolderName;

    @NotBlank(message = "Account number is required")
    @Size(max = 50)
    private String accountNumber;

    @NotBlank(message = "Bank name is required")
    @Size(max = 150)
    private String bankName;

    @NotBlank(message = "IFSC or SWIFT code is required")
    @Size(max = 20)
    private String ifscOrSwiftCode;
}
