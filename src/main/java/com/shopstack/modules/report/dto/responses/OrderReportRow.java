package com.shopstack.modules.report.dto.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class OrderReportRow {
    private UUID orderId;
    private String status;
    private Double amount;
    private LocalDateTime placedAt;
}