package com.shopstack.modules.report.service;

import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.entity.OrderItem;
import com.shopstack.modules.order.repository.OrderItemRepository;
import com.shopstack.modules.order.repository.OrderRepository;
import com.shopstack.modules.report.dto.responses.FinancialReportRow;
import com.shopstack.modules.report.dto.responses.OrderReportRow;
import com.shopstack.modules.report.dto.responses.SalesReportRow;
import com.shopstack.modules.report.dto.responses.VendorEarningsAnalyticsResponse;
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
import java.util.UUID;
import java.util.stream.Collectors;
import com.shopstack.modules.product.entity.Product;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final VendorRepository vendorRepository;
    private final com.shopstack.modules.order.repository.CommissionLedgerRepository commissionLedgerRepository;
    private final com.shopstack.modules.product.repository.ProductRepository productRepository;

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
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);

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

        public VendorEarningsAnalyticsResponse getVendorEarningsAnalytics(Long vendorId, String range) {
        LocalDate to = LocalDate.now();
        LocalDate from = switch (range == null ? "30d" : range) {
            case "90d" -> to.minusDays(90);
            case "1y" -> to.minusYears(1);
            default -> to.minusDays(30);
        };

        LocalDateTime start = from.atStartOfDay();
        LocalDateTime end = to.atTime(23, 59, 59);

        List<com.shopstack.modules.order.entity.CommissionLedger> vendorLedger = commissionLedgerRepository.findAllByVendorIdAndRange(vendorId, start, end);

        BigDecimal grossSales = vendorLedger.stream()
                .filter(l -> l.getTransactionType() == com.shopstack.modules.order.enums.LedgerTransactionType.COMMISSION)
                .map(com.shopstack.modules.order.entity.CommissionLedger::getGrossAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal platformCommission = vendorLedger.stream()
                .filter(l -> l.getTransactionType() == com.shopstack.modules.order.enums.LedgerTransactionType.COMMISSION)
                .map(com.shopstack.modules.order.entity.CommissionLedger::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal refundReversalAmount = vendorLedger.stream()
                .filter(l -> l.getTransactionType() == com.shopstack.modules.order.enums.LedgerTransactionType.REFUND_REVERSAL)
                .map(com.shopstack.modules.order.entity.CommissionLedger::getCommissionAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .abs()
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal netVendorEarnings = vendorLedger.stream()
                .map(com.shopstack.modules.order.entity.CommissionLedger::getVendorAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        long completedOrderCount = vendorLedger.stream()
                .map(com.shopstack.modules.order.entity.CommissionLedger::getOrderId)
                .distinct()
                .count();

        BigDecimal avgOrderValue = completedOrderCount > 0
                ? grossSales.divide(BigDecimal.valueOf(completedOrderCount), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        List<VendorEarningsAnalyticsResponse.TrendPoint> trend = new ArrayList<>();
        int days = switch (range == null ? "30d" : range) {
            case "90d" -> 90;
            case "1y" -> 365;
            default -> 30;
        };
        for (int i = days - 1; i >= 0; i--) {
            LocalDate day = to.minusDays(i);
            BigDecimal dayEarnings = vendorLedger.stream()
                    .filter(l -> l.getCreatedAt() != null && l.getCreatedAt().toLocalDate().equals(day))
                    .map(com.shopstack.modules.order.entity.CommissionLedger::getVendorAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .setScale(2, RoundingMode.HALF_UP);
            trend.add(VendorEarningsAnalyticsResponse.TrendPoint.builder()
                    .label(day.getMonth().toString().substring(0, 3) + " " + day.getDayOfMonth())
                    .earnings(dayEarnings)
                    .build());
        }

        Map<UUID, BigDecimal> productRevenue = vendorLedger.stream()
                .filter(l -> l.getTransactionType() == com.shopstack.modules.order.enums.LedgerTransactionType.COMMISSION)
                .collect(Collectors.groupingBy(com.shopstack.modules.order.entity.CommissionLedger::getProductId,
                        Collectors.mapping(com.shopstack.modules.order.entity.CommissionLedger::getGrossAmount, Collectors.reducing(BigDecimal.ZERO, BigDecimal::add))));

        List<UUID> orderItemIds = vendorLedger.stream()
                .map(com.shopstack.modules.order.entity.CommissionLedger::getOrderItemId)
                .toList();
        List<OrderItem> orderItems = orderItemRepository.findAllById(orderItemIds);
        Map<UUID, Integer> orderItemQtys = orderItems.stream()
                .collect(Collectors.toMap(OrderItem::getId, OrderItem::getQuantity, (a, b) -> a));

        List<VendorEarningsAnalyticsResponse.TopProductPoint> topProducts = productRevenue.entrySet().stream()
                .map(entry -> {
                    String name = "Product (" + entry.getKey().toString().substring(0, 8) + ")";
                    try {
                        Product prod = productRepository.findById(entry.getKey()).orElse(null);
                        if (prod != null) {
                            name = prod.getProductName();
                        }
                    } catch (Exception ignored) {}

                    long unitsSold = vendorLedger.stream()
                            .filter(l -> entry.getKey().equals(l.getProductId()) && l.getTransactionType() == com.shopstack.modules.order.enums.LedgerTransactionType.COMMISSION)
                            .mapToLong(l -> orderItemQtys.getOrDefault(l.getOrderItemId(), 1))
                            .sum();

                    return VendorEarningsAnalyticsResponse.TopProductPoint.builder()
                            .productName(name)
                            .revenue(entry.getValue())
                            .unitsSold(unitsSold)
                            .build();
                })
                .sorted((a, b) -> b.getRevenue().compareTo(a.getRevenue()))
                .limit(5)
                .toList();

        return VendorEarningsAnalyticsResponse.builder()
                .totalEarnings(grossSales)
                .netPayout(netVendorEarnings)
                .avgOrderValue(avgOrderValue)
                .totalOrders(completedOrderCount)
                .trend(trend)
                .topProducts(topProducts)
                .grossSales(grossSales)
                .platformCommission(platformCommission)
                .refundReversalAmount(refundReversalAmount)
                .netVendorEarnings(netVendorEarnings)
                .build();
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
        BigDecimal totalRevenue = sales.stream().map(SalesReportRow::getRevenue).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal totalCommission = vendorRows.stream().map(VendorReportRow::getCommissionEarned).reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);

        BigDecimal totalGst = totalRevenue.multiply(BigDecimal.valueOf(0.18))
                .divide(BigDecimal.valueOf(1.18), 2, RoundingMode.HALF_UP);

        BigDecimal netRevenue = totalRevenue.subtract(totalCommission).setScale(2, RoundingMode.HALF_UP);

        return FinancialReportRow.builder()
                .totalOrders(totalOrders)
                .totalRevenue(totalRevenue)
                .totalGstCollected(totalGst)
                .totalCommissionEarned(totalCommission)
                .netRevenue(netRevenue)
                .build();
    }
}