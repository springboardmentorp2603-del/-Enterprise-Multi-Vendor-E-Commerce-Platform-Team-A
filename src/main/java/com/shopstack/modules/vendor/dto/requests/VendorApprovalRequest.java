package com.shopstack.modules.vendor.dto.requests;

import com.shopstack.common.enums.VendorStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorApprovalRequest {

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @NotNull(message = "New status is required")
    private VendorStatus newStatus;

    @NotNull(message = "Reviewing admin ID is required")
    private UUID reviewedByUserId;

    private String remarks;
}
