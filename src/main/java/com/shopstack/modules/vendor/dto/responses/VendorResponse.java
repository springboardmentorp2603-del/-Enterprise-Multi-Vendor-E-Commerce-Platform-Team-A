package com.shopstack.modules.vendor.dto.responses;

import com.shopstack.common.enums.VendorStatus;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorResponse {

    private Long vendorId;
    private UUID userId;
    private String businessName;
    private String businessEmail;
    private String businessPhone;
    private String gstNumber;
    private String panNumber;
    private String businessType;
    private String logoUrl;
    private String description;
    private VendorStatus status;
    private BigDecimal commissionRate;
    private BigDecimal rating;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<VendorAddressResponse> addresses;
    private List<VendorBankDetailsResponse> bankDetails;
    private List<VendorDocumentResponse> documents;
}
