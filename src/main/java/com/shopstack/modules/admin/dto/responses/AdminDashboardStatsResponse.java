package com.shopstack.modules.admin.dto.responses;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AdminDashboardStatsResponse {

    private long pendingVendorApprovals;
    private long activeVendors;
    private long totalVendors;

    private long pendingProductApprovals;
    private long approvedProducts;
    private long totalProducts;

    private long totalCustomers;
}
