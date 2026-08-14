package com.shopstack.modules.report.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class VendorEarningsAnalyticsResponse {
    private BigDecimal totalEarnings;
    private BigDecimal netPayout;
    private BigDecimal avgOrderValue;
    private long totalOrders;
    private List<TrendPoint> trend;
    private List<TopProductPoint> topProducts;

    private BigDecimal grossSales;
    private BigDecimal platformCommission;
    private BigDecimal refundReversalAmount;
    private BigDecimal netVendorEarnings;

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TrendPoint {
        private String label;
        private BigDecimal earnings;
    }

    @Getter
    @Builder
    @AllArgsConstructor
    public static class TopProductPoint {
        private String productName;
        private BigDecimal revenue;
        private long unitsSold;
    }
}
