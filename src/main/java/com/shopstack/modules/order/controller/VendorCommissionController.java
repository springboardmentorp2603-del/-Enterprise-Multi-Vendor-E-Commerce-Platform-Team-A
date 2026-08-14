package com.shopstack.modules.order.controller;

import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.order.entity.CommissionLedger;
import com.shopstack.modules.order.enums.LedgerTransactionType;
import com.shopstack.modules.order.service.CommissionService;
import com.shopstack.modules.vendor.entity.Vendor;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendors/commission")
@RequiredArgsConstructor
@PreAuthorize("hasRole('VENDOR')")
public class VendorCommissionController {

    private final CommissionService commissionService;
    private final SecurityUtil securityUtil;

    @GetMapping("/ledger")
    public ResponseEntity<Page<CommissionLedger>> getCommissionLedger(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo,
            @RequestParam(required = false) String transactionType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) UUID orderId,
            @RequestParam(required = false) UUID productId,
            Pageable pageable) {

        Vendor vendor = securityUtil.getCurrentVendor();
        if (vendor == null) {
            throw new IllegalArgumentException("Authenticated user is not registered as a Vendor");
        }

        LocalDateTime fromTime = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime toTime = dateTo != null ? dateTo.atTime(23, 59, 59) : null;

        LedgerTransactionType typeEnum = null;
        if (transactionType != null && !transactionType.isBlank()) {
            typeEnum = LedgerTransactionType.valueOf(transactionType.toUpperCase());
        }

        Page<CommissionLedger> ledger = commissionService.getVendorCommissionLedger(
                vendor.getVendorId(),
                fromTime,
                toTime,
                typeEnum,
                status,
                orderId,
                productId,
                pageable
        );

        return ResponseEntity.ok(ledger);
    }

    @GetMapping("/summary")
    public ResponseEntity<Map<String, Object>> getCommissionSummary() {
        Vendor vendor = securityUtil.getCurrentVendor();
        if (vendor == null) {
            throw new IllegalArgumentException("Authenticated user is not registered as a Vendor");
        }

        return ResponseEntity.ok(commissionService.getVendorCommissionSummary(vendor.getVendorId()));
    }
}
