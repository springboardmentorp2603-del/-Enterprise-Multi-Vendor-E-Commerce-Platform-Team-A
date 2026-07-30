package com.shopstack.modules.vendor.repository;

import com.shopstack.common.enums.DocumentType;
import com.shopstack.modules.vendor.entity.VendorDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VendorDocumentRepository extends JpaRepository<VendorDocument, Long> {

    List<VendorDocument> findByVendor_VendorId(Long vendorId);

    List<VendorDocument> findByVendor_VendorIdAndDocumentType(Long vendorId, DocumentType documentType);

    List<VendorDocument> findByVendor_VendorIdAndVerifiedFalse(Long vendorId);

    long countByVendor_VendorIdAndVerifiedTrue(Long vendorId);
}