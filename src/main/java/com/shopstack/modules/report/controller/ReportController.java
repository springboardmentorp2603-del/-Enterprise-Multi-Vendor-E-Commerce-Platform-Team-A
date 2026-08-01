package com.shopstack.modules.report.controller;

import com.shopstack.common.response.ApiResponse;
import com.shopstack.common.response.ApiResponseBuilder;
import com.shopstack.modules.report.dto.responses.FinancialReportRow;
import com.shopstack.modules.report.dto.responses.OrderReportRow;
import com.shopstack.modules.report.dto.responses.SalesReportRow;
import com.shopstack.modules.report.dto.responses.VendorReportRow;
import com.shopstack.modules.report.service.ReportExportService;
import com.shopstack.modules.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class ReportController {

    private final ReportService reportService;
    private final ReportExportService reportExportService;

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> getAllReports() {
        LocalDate to = LocalDate.now();
        LocalDate from = to.minusDays(30);

        FinancialReportRow financial = reportService.getFinancialReport(from, to);

        List<Map<String, Object>> summary = new java.util.ArrayList<>();
        summary.add(Map.of("title", "Total Orders", "value", financial.getTotalOrders()));
        summary.add(Map.of("title", "Total Revenue", "value", financial.getTotalRevenue()));
        summary.add(Map.of("title", "GST Collected", "value", financial.getTotalGstCollected()));
        summary.add(Map.of("title", "Commission Earned", "value", financial.getTotalCommissionEarned()));
        summary.add(Map.of("title", "Net Revenue", "value", financial.getNetRevenue()));

        return ApiResponseBuilder.success("Reports fetched", summary);
    }

    @GetMapping("/sales")
    public ApiResponse<List<SalesReportRow>> salesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponseBuilder.success("Sales report fetched", reportService.getSalesReport(from, to));
    }

    @GetMapping("/vendors")
    public ApiResponse<List<VendorReportRow>> vendorReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponseBuilder.success("Vendor report fetched", reportService.getVendorReport(from, to));
    }

    @GetMapping("/orders")
    public ApiResponse<List<OrderReportRow>> orderReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(required = false) String status) {
        return ApiResponseBuilder.success("Order report fetched", reportService.getOrderReport(from, to, status));
    }

    @GetMapping("/financial")
    public ApiResponse<FinancialReportRow> financialReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ApiResponseBuilder.success("Financial report fetched", reportService.getFinancialReport(from, to));
    }

    @GetMapping("/sales/export")
    public ApiResponse<Map<String, String>> exportSalesReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam String format) {
        List<SalesReportRow> rows = reportService.getSalesReport(from, to);
        String path = "excel".equalsIgnoreCase(format)
                ? reportExportService.generateSalesReportExcel(rows)
                : reportExportService.generateSalesReportPdf(rows, from, to);
        return ApiResponseBuilder.success("Report generated", Map.of("path", path));
    }

    @GetMapping("/vendors/export")
    public ApiResponse<Map<String, String>> exportVendorReport(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        List<VendorReportRow> rows = reportService.getVendorReport(from, to);
        String path = reportExportService.generateVendorReportExcel(rows);
        return ApiResponseBuilder.success("Report generated", Map.of("path", path));
    }
}