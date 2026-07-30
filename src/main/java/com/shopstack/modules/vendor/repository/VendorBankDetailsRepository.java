package com.shopstack.modules.vendor.repository;

import com.shopstack.modules.vendor.entity.VendorBankDetails;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorBankDetailsRepository extends JpaRepository<VendorBankDetails, Long> {

    List<VendorBankDetails> findByVendor_VendorId(Long vendorId);

    Optional<VendorBankDetails> findByVendor_VendorIdAndIsVerifiedTrue(Long vendorId);

    boolean existsByVendor_VendorIdAndAccountNumber(Long vendorId, String accountNumber);
}