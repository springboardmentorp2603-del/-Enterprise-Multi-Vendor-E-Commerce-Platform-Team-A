package com.shopstack.modules.admin.service;

import com.shopstack.common.enums.ProductApprovalStatus;
import com.shopstack.common.enums.VendorStatus;
import com.shopstack.modules.admin.dto.responses.AdminDashboardStatsResponse;
import com.shopstack.modules.customer.repository.CustomerRepository;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminDashboardService {

    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;

    public AdminDashboardStatsResponse getStats() {
        return AdminDashboardStatsResponse.builder()
                .pendingVendorApprovals(vendorRepository.countByStatus(VendorStatus.PENDING))
                .activeVendors(vendorRepository.countByStatus(VendorStatus.ACTIVE))
                .totalVendors(vendorRepository.count())
                .pendingProductApprovals(productRepository.countByApprovalStatus(ProductApprovalStatus.PENDING))
                .approvedProducts(productRepository.countByApprovalStatus(ProductApprovalStatus.APPROVED))
                .totalProducts(productRepository.count())
                .totalCustomers(customerRepository.count())
                .build();
    }
}
