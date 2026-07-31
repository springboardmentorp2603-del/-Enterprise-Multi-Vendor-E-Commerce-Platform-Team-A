package com.shopstack.modules.report.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class VendorReportRow {
    private Long vendorId;
    private String businessName;
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal commissionEarned;
}