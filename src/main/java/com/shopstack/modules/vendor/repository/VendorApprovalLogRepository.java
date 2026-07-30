package com.shopstack.modules.vendor.repository;

import com.shopstack.modules.vendor.entity.VendorApprovalLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorApprovalLogRepository extends JpaRepository<VendorApprovalLog, Long> {

    List<VendorApprovalLog> findByVendor_VendorIdOrderByReviewedAtDesc(Long vendorId);

    Page<VendorApprovalLog> findByVendor_VendorId(Long vendorId, Pageable pageable);

    Optional<VendorApprovalLog> findFirstByVendor_VendorIdOrderByReviewedAtDesc(Long vendorId);

    List<VendorApprovalLog> findByReviewedBy_Id(UUID adminUserId);
}
