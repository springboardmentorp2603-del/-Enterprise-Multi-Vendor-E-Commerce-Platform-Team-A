package com.shopstack.modules.vendor.dto.responses;

import com.shopstack.common.enums.VendorStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorApprovalLogResponse {

    private Long logId;
    private VendorStatus previousStatus;
    private VendorStatus newStatus;
    private String reviewedByName;
    private String remarks;
    private LocalDateTime reviewedAt;
}