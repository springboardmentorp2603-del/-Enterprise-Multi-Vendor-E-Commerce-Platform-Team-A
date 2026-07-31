package com.shopstack.modules.report.service;

import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.entity.OrderItem;
import com.shopstack.modules.order.repository.OrderItemRepository;
import com.shopstack.modules.order.repository.OrderRepository;
import com.shopstack.modules.report.dto.responses.FinancialReportRow;
import com.shopstack.modules.report.dto.responses.OrderReportRow;
import com.shopstack.modules.report.dto.responses.SalesReportRow;
import com.shopstack.modules.report.dto.responses.VendorReportRow;
import com.shopstack.modules.vendor.entity.Vendor;
import com.shopstack.modules.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VendorRepository vendorRepository;

    public List<SalesReportRow> getSalesReport(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        List<Order> orders = orderRepository.findAllInRange(start, end);

        Map<LocalDate, List<Order>> grouped = orders.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<SalesReportRow> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<Order>> entry : grouped.entrySet()) {
            BigDecimal revenue = entry.getValue().stream()
                    .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                    .map(o -> BigDecimal.valueOf(o.getTotalAmount()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            rows.add(SalesReportRow.builder()
                    .date(entry.getKey())
                    .orderCount(entry.getValue().size())
                    .revenue(revenue)
                    .build());
        }
        rows.sort(Comparator.comparing(SalesReportRow::getDate));
        return rows;
    }

    public List<VendorReportRow> getVendorReport(LocalDate from, LocalDate to) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        List<OrderItem> items = orderItemRepository.findAllInRange(start, end);

        Map<Long, List<OrderItem>> byVendor = items.stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getVendorId() != null)
                .collect(Collectors.groupingBy(item -> item.getProduct().getVendorId()));

        List<VendorReportRow> rows = new ArrayList<>();
        for (Map.Entry<Long, List<OrderItem>> entry : byVendor.entrySet()) {
            Vendor vendor = vendorRepository.findById(entry.getKey()).orElse(null);
            if (vendor == null) continue;

            BigDecimal revenue = entry.getValue().stream()
                    .map(item -> BigDecimal.valueOf(item.getPrice() * item.getQuantity()))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal commission = vendor.getCommissionRate() != null
                    ? revenue.multiply(vendor.getCommissionRate()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO;

            rows.add(VendorReportRow.builder()
                    .vendorId(vendor.getVendorId())
                    .businessName(vendor.getBusinessName())
                    .totalOrders((long) entry.getValue().size())
                    .totalRevenue(revenue)
                    .commissionEarned(commission)
                    .build());
        }
        rows.sort(Comparator.comparing(VendorReportRow::getTotalRevenue).reversed());
        return rows;
    }

    public List<OrderReportRow> getOrderReport(LocalDate from, LocalDate to, String statusFilter) {
        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        return orderRepository.findAllInRange(start, end).stream()
                .filter(o -> statusFilter == null || statusFilter.isBlank()
                        || statusFilter.equalsIgnoreCase(o.getStatus()))
                .map(o -> OrderReportRow.builder()
                        .orderId(o.getId())
                        .status(o.getStatus())
                        .amount(o.getTotalAmount())
                        .placedAt(o.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    public FinancialReportRow getFinancialReport(LocalDate from, LocalDate to) {
        List<SalesReportRow> sales = getSalesReport(from, to);
        List<VendorReportRow> vendorRows = getVendorReport(from, to);

        long totalOrders = sales.stream().mapToLong(SalesReportRow::getOrderCount).sum();
        BigDecimal totalRevenue = sales.stream().map(SalesReportRow::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCommission = vendorRows.stream().map(VendorReportRow::getCommissionEarned).reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalGst = totalRevenue.multiply(BigDecimal.valueOf(0.18))
                .divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);

        BigDecimal netRevenue = totalRevenue.subtract(totalCommission);

        return FinancialReportRow.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalGstCollected(totalGst)
                .totalCommissionEarned(totalCommission)
                .netRevenue(netRevenue)
                .build();
    }
}