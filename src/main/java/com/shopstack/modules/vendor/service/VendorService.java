package com.shopstack.modules.vendor.service;

import com.shopstack.common.enums.VendorStatus;
import com.shopstack.modules.vendor.dto.requests.*;
import com.shopstack.modules.vendor.dto.responses.VendorApprovalLogResponse;
import com.shopstack.modules.vendor.dto.responses.VendorResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface VendorService {

    VendorResponse registerVendor(VendorRegistrationRequest request);

    VendorResponse getVendorById(Long vendorId);

    VendorResponse getVendorByUserId(UUID userId);

    Page<VendorResponse> listVendors(VendorStatus status, Pageable pageable);

    Page<VendorResponse> searchVendors(String keyword, Pageable pageable);

    VendorResponse updateVendor(Long vendorId, VendorUpdateRequest request);

    void deleteVendor(Long vendorId);

    VendorResponse addAddress(Long vendorId, VendorAddressRequest request);

    VendorResponse addBankDetails(Long vendorId, VendorBankDetailsRequest request);

    VendorResponse uploadDocument(Long vendorId, VendorDocumentUploadRequest request);

    VendorResponse reviewVendor(VendorApprovalRequest request);

    List<VendorApprovalLogResponse> getApprovalHistory(Long vendorId);
}
