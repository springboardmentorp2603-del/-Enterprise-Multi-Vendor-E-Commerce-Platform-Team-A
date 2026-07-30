package com.shopstack.modules.vendor.dto.responses;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorBankDetailsResponse {

    private Long bankDetailId;
    private String accountHolderName;

    /** Masked, e.g. "XXXXXXXX1234" — never return the full account number to the client. */
    private String maskedAccountNumber;

    private String bankName;
    private Boolean isVerified;
}