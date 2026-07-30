package com.shopstack.modules.vendor.mapper;

import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.vendor.dto.requests.VendorAddressRequest;
import com.shopstack.modules.vendor.dto.requests.VendorBankDetailsRequest;
import com.shopstack.modules.vendor.dto.requests.VendorDocumentUploadRequest;
import com.shopstack.modules.vendor.dto.responses.*;
import com.shopstack.modules.vendor.entity.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class VendorMapper {

    // ---------- Vendor ----------

    public VendorResponse toResponse(Vendor vendor) {
        return VendorResponse.builder()
                .vendorId(vendor.getVendorId())
                .userId(vendor.getUser() != null ? vendor.getUser().getId() : null)
                .businessName(vendor.getBusinessName())
                .businessEmail(vendor.getBusinessEmail())
                .businessPhone(vendor.getBusinessPhone())
                .gstNumber(vendor.getGstNumber())
                .panNumber(vendor.getPanNumber())
                .businessType(vendor.getBusinessType())
                .logoUrl(vendor.getLogoUrl())
                .description(vendor.getDescription())
                .status(vendor.getStatus())
                .commissionRate(vendor.getCommissionRate())
                .rating(vendor.getRating())
                .createdAt(vendor.getCreatedAt())
                .updatedAt(vendor.getUpdatedAt())
                .addresses(toAddressResponseList(vendor.getAddresses()))
                .bankDetails(toBankDetailsResponseList(vendor.getBankDetails()))
                .documents(toDocumentResponseList(vendor.getDocuments()))
                .build();
    }

    public List<VendorResponse> toResponseList(List<Vendor> vendors) {
        return vendors.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ---------- Address ----------

    public VendorAddress toEntity(VendorAddressRequest dto, Vendor vendor) {
        return VendorAddress.builder()
                .vendor(vendor)
                .addressType(dto.getAddressType())
                .addressLine1(dto.getAddressLine1())
                .addressLine2(dto.getAddressLine2())
                .city(dto.getCity())
                .state(dto.getState())
                .country(dto.getCountry())
                .postalCode(dto.getPostalCode())
                .isDefault(dto.getIsDefault() != null ? dto.getIsDefault() : false)
                .build();
    }

    public VendorAddressResponse toResponse(VendorAddress address) {
        return VendorAddressResponse.builder()
                .addressId(address.getAddressId())
                .addressType(address.getAddressType())
                .addressLine1(address.getAddressLine1())
                .addressLine2(address.getAddressLine2())
                .city(address.getCity())
                .state(address.getState())
                .country(address.getCountry())
                .postalCode(address.getPostalCode())
                .isDefault(address.getIsDefault())
                .build();
    }

    public List<VendorAddressResponse> toAddressResponseList(List<VendorAddress> addresses) {
        if (addresses == null) return List.of();
        return addresses.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ---------- Bank Details ----------

    public VendorBankDetails toEntity(VendorBankDetailsRequest dto, Vendor vendor) {
        return VendorBankDetails.builder()
                .vendor(vendor)
                .accountHolderName(dto.getAccountHolderName())
                .accountNumber(dto.getAccountNumber())
                .bankName(dto.getBankName())
                .ifscOrSwiftCode(dto.getIfscOrSwiftCode())
                .isVerified(false)
                .build();
    }

    public VendorBankDetailsResponse toResponse(VendorBankDetails bank) {
        return VendorBankDetailsResponse.builder()
                .bankDetailId(bank.getBankDetailId())
                .accountHolderName(bank.getAccountHolderName())
                .maskedAccountNumber(maskAccountNumber(bank.getAccountNumber()))
                .bankName(bank.getBankName())
                .isVerified(bank.getIsVerified())
                .build();
    }

    public List<VendorBankDetailsResponse> toBankDetailsResponseList(List<VendorBankDetails> bankDetails) {
        if (bankDetails == null) return List.of();
        return bankDetails.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() <= 4) return "****";
        return "X".repeat(accountNumber.length() - 4) + accountNumber.substring(accountNumber.length() - 4);
    }

    // ---------- Documents ----------

    public VendorDocument toEntity(VendorDocumentUploadRequest dto, Vendor vendor) {
        return VendorDocument.builder()
                .vendor(vendor)
                .documentType(dto.getDocumentType())
                .documentUrl(dto.getDocumentUrl())
                .verified(false)
                .build();
    }

    public VendorDocumentResponse toResponse(VendorDocument document) {
        return VendorDocumentResponse.builder()
                .documentId(document.getDocumentId())
                .documentType(document.getDocumentType())
                .documentUrl(document.getDocumentUrl())
                .verified(document.getVerified())
                .uploadedAt(document.getUploadedAt())
                .build();
    }

    public List<VendorDocumentResponse> toDocumentResponseList(List<VendorDocument> documents) {
        if (documents == null) return List.of();
        return documents.stream().map(this::toResponse).collect(Collectors.toList());
    }

    // ---------- Approval Log ----------

    public VendorApprovalLogResponse toResponse(VendorApprovalLog log) {
        return VendorApprovalLogResponse.builder()
                .logId(log.getLogId())
                .previousStatus(log.getPreviousStatus())
                .newStatus(log.getNewStatus())
                .reviewedByName(formatUserName(log.getReviewedBy()))
                .remarks(log.getRemarks())
                .reviewedAt(log.getReviewedAt())
                .build();
    }

    public List<VendorApprovalLogResponse> toApprovalLogResponseList(List<VendorApprovalLog> logs) {
        if (logs == null) return List.of();
        return logs.stream().map(this::toResponse).collect(Collectors.toList());
    }

    private String formatUserName(User user) {
        if (user == null) return "SYSTEM";
        String fullName = String.join(" ",
                user.getFirstName() == null ? "" : user.getFirstName(),
                user.getLastName() == null ? "" : user.getLastName()).trim();
        return fullName.isBlank() ? user.getEmail() : fullName;
    }
}
