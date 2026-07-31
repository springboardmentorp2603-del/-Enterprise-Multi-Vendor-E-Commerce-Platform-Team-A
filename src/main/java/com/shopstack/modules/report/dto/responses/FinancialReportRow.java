package com.shopstack.modules.report.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
@AllArgsConstructor
public class FinancialReportRow {
    private long totalOrders;
    private BigDecimal totalRevenue;
    private BigDecimal totalGstCollected;
    private BigDecimal totalCommissionEarned;
    private BigDecimal netRevenue;
}