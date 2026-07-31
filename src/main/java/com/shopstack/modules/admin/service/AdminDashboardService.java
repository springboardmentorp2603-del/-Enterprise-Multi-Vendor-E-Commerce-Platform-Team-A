package com.shopstack.modules.admin.service;

import com.shopstack.common.enums.ProductApprovalStatus;
import com.shopstack.common.enums.VendorStatus;
import com.shopstack.modules.admin.dto.responses.AdminDashboardStatsResponse;
import com.shopstack.modules.customer.repository.CustomerRepository;
import com.shopstack.modules.order.entity.OrderItem;
import com.shopstack.modules.order.repository.OrderItemRepository;
import com.shopstack.modules.order.repository.OrderRepository;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.vendor.entity.Vendor;
import com.shopstack.modules.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    public AdminDashboardStatsResponse getStats() {
        BigDecimal totalRevenue = orderRepository.sumTotalRevenue() != null
                ? BigDecimal.valueOf(orderRepository.sumTotalRevenue())
                : BigDecimal.ZERO;

        return AdminDashboardStatsResponse.builder()
                .pendingVendorApprovals(vendorRepository.countByStatus(VendorStatus.PENDING))
                .activeVendors(vendorRepository.countByStatus(VendorStatus.ACTIVE))
                .totalVendors(vendorRepository.count())
                .pendingProductApprovals(productRepository.countByApprovalStatus(ProductApprovalStatus.PENDING))
                .approvedProducts(productRepository.countByApprovalStatus(ProductApprovalStatus.APPROVED))
                .totalProducts(productRepository.count())
                .totalCustomers(customerRepository.count())
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus("PENDING"))
                .totalRevenue(totalRevenue)
                .totalCommissionEarned(calculateTotalCommissionEarned())
                .build();
    }

    private BigDecimal calculateTotalCommissionEarned() {
        List<OrderItem> allItems = orderItemRepository.findAll();

        Map<Long, BigDecimal> revenueByVendor = allItems.stream()
                .filter(item -> item.getProduct() != null && item.getProduct().getVendorId() != null)
                .collect(Collectors.groupingBy(
                        item -> item.getProduct().getVendorId(),
                        Collectors.reducing(
                                BigDecimal.ZERO,
                                item -> BigDecimal.valueOf(item.getPrice() * item.getQuantity()),
                                BigDecimal::add
                        )
                ));

        BigDecimal totalCommission = BigDecimal.ZERO;
        for (Map.Entry<Long, BigDecimal> entry : revenueByVendor.entrySet()) {
            Vendor vendor = vendorRepository.findById(entry.getKey()).orElse(null);
            if (vendor != null && vendor.getCommissionRate() != null) {
                BigDecimal commission = entry.getValue()
                        .multiply(vendor.getCommissionRate())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
                totalCommission = totalCommission.add(commission);
            }
        }
        return totalCommission;
    }
}