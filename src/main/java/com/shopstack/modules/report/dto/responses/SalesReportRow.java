package com.shopstack.modules.report.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
@AllArgsConstructor
public class SalesReportRow {
    private LocalDate date;
    private long orderCount;
    private BigDecimal revenue;
}