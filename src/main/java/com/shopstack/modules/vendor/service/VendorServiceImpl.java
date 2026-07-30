package com.shopstack.modules.vendor.service;

import com.shopstack.common.enums.VendorStatus;
import com.shopstack.common.exception.DuplicateResourceException;
import com.shopstack.common.exception.InvalidVendorStateException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.repository.UserRepository;
import com.shopstack.modules.vendor.dto.requests.*;
import com.shopstack.modules.vendor.dto.responses.VendorApprovalLogResponse;
import com.shopstack.modules.vendor.dto.responses.VendorResponse;
import com.shopstack.modules.vendor.entity.Vendor;
import com.shopstack.modules.vendor.entity.VendorAddress;
import com.shopstack.modules.vendor.entity.VendorApprovalLog;
import com.shopstack.modules.vendor.mapper.VendorMapper;
import com.shopstack.modules.vendor.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class VendorServiceImpl implements VendorService {

    private final VendorRepository vendorRepository;
    //private final VendorAddressRepository addressRepository;
    private final VendorBankDetailsRepository bankDetailsRepository;
    //private final VendorDocumentRepository documentRepository;
    private final VendorApprovalLogRepository approvalLogRepository;
    private final UserRepository userRepository;
    private final VendorMapper mapper;

    @Override
    public VendorResponse registerVendor(VendorRegistrationRequest request) {
        if (vendorRepository.existsByBusinessEmail(request.getBusinessEmail())) {
            throw new DuplicateResourceException(
                    "A vendor with business email '" + request.getBusinessEmail() + "' already exists");
        }
        if (request.getGstNumber() != null && vendorRepository.existsByGstNumber(request.getGstNumber())) {
            throw new DuplicateResourceException(
                    "A vendor with GST number '" + request.getGstNumber() + "' already exists");
        }

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        Vendor vendor = Vendor.builder()
                .user(user)
                .businessName(request.getBusinessName())
                .businessEmail(request.getBusinessEmail())
                .businessPhone(request.getBusinessPhone())
                .gstNumber(request.getGstNumber())
                .panNumber(request.getPanNumber())
                .businessType(request.getBusinessType())
                .logoUrl(request.getLogoUrl())
                .description(request.getDescription())
                .status(VendorStatus.PENDING)
                .build();

        if (request.getAddresses() != null) {
            request.getAddresses().forEach(a -> vendor.getAddresses().add(mapper.toEntity(a, vendor)));
        }
        if (request.getBankDetails() != null) {
            request.getBankDetails().forEach(b -> vendor.getBankDetails().add(mapper.toEntity(b, vendor)));
        }
        if (request.getDocuments() != null) {
            request.getDocuments().forEach(d -> vendor.getDocuments().add(mapper.toEntity(d, vendor)));
        }

        Vendor saved = vendorRepository.save(vendor);

        // Initial audit trail entry
        VendorApprovalLog initialLog = VendorApprovalLog.builder()
                .vendor(saved)
                .previousStatus(null)
                .newStatus(VendorStatus.PENDING)
                .remarks("Vendor registration submitted")
                .build();
        approvalLogRepository.save(initialLog);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public VendorResponse getVendorById(Long vendorId) {
        return mapper.toResponse(findVendorOrThrow(vendorId));
    }

    @Override
    @Transactional(readOnly = true)
    public VendorResponse getVendorByUserId(UUID userId) {
        Vendor vendor = vendorRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No vendor profile found for user id: " + userId));
        return mapper.toResponse(vendor);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorResponse> listVendors(VendorStatus status, Pageable pageable) {
        Page<Vendor> vendors = (status != null)
                ? vendorRepository.findByStatus(status, pageable)
                : vendorRepository.findAll(pageable);
        return vendors.map(mapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VendorResponse> searchVendors(String keyword, Pageable pageable) {
        return vendorRepository.searchByKeyword(keyword, pageable).map(mapper::toResponse);
    }

    @Override
    public VendorResponse updateVendor(Long vendorId, VendorUpdateRequest request) {
        Vendor vendor = findVendorOrThrow(vendorId);

        if (request.getBusinessName() != null) vendor.setBusinessName(request.getBusinessName());
        if (request.getBusinessEmail() != null) {
            if (!request.getBusinessEmail().equalsIgnoreCase(vendor.getBusinessEmail())
                    && vendorRepository.existsByBusinessEmail(request.getBusinessEmail())) {
                throw new DuplicateResourceException(
                        "A vendor with business email '" + request.getBusinessEmail() + "' already exists");
            }
            vendor.setBusinessEmail(request.getBusinessEmail());
        }
        if (request.getBusinessPhone() != null) vendor.setBusinessPhone(request.getBusinessPhone());
        if (request.getBusinessType() != null) vendor.setBusinessType(request.getBusinessType());
        if (request.getLogoUrl() != null) vendor.setLogoUrl(request.getLogoUrl());
        if (request.getDescription() != null) vendor.setDescription(request.getDescription());

        return mapper.toResponse(vendorRepository.save(vendor));
    }

    @Override
    public void deleteVendor(Long vendorId) {
        Vendor vendor = findVendorOrThrow(vendorId);
        vendorRepository.delete(vendor);
    }

    @Override
    public VendorResponse addAddress(Long vendorId, VendorAddressRequest request) {
        Vendor vendor = findVendorOrThrow(vendorId);
        VendorAddress address = mapper.toEntity(request, vendor);

        if (Boolean.TRUE.equals(address.getIsDefault())) {
            vendor.getAddresses().forEach(a -> a.setIsDefault(false));
        }

        vendor.getAddresses().add(address);
        return mapper.toResponse(vendorRepository.save(vendor));
    }

    @Override
    public VendorResponse addBankDetails(Long vendorId, VendorBankDetailsRequest request) {
        Vendor vendor = findVendorOrThrow(vendorId);

        if (bankDetailsRepository.existsByVendor_VendorIdAndAccountNumber(vendorId, request.getAccountNumber())) {
            throw new DuplicateResourceException("This bank account is already registered for the vendor");
        }

        vendor.getBankDetails().add(mapper.toEntity(request, vendor));
        return mapper.toResponse(vendorRepository.save(vendor));
    }

    @Override
    public VendorResponse uploadDocument(Long vendorId, VendorDocumentUploadRequest request) {
        Vendor vendor = findVendorOrThrow(vendorId);
        vendor.getDocuments().add(mapper.toEntity(request, vendor));
        return mapper.toResponse(vendorRepository.save(vendor));
    }

    @Override
    public VendorResponse reviewVendor(VendorApprovalRequest request) {
        Vendor vendor = findVendorOrThrow(request.getVendorId());

        if (vendor.getStatus() == request.getNewStatus()) {
            throw new InvalidVendorStateException(
                    "Vendor is already in status: " + request.getNewStatus());
        }

        User admin = userRepository.findById(request.getReviewedByUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Reviewing admin not found with id: " + request.getReviewedByUserId()));

        VendorStatus previousStatus = vendor.getStatus();
        vendor.setStatus(request.getNewStatus());
        Vendor saved = vendorRepository.save(vendor);

        VendorApprovalLog log = VendorApprovalLog.builder()
                .vendor(saved)
                .reviewedBy(admin)
                .previousStatus(previousStatus)
                .newStatus(request.getNewStatus())
                .remarks(request.getRemarks())
                .build();
        approvalLogRepository.save(log);

        return mapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<VendorApprovalLogResponse> getApprovalHistory(Long vendorId) {
        findVendorOrThrow(vendorId); // ensures vendor exists
        List<VendorApprovalLog> logs =
                approvalLogRepository.findByVendor_VendorIdOrderByReviewedAtDesc(vendorId);
        return mapper.toApprovalLogResponseList(logs);
    }

    private Vendor findVendorOrThrow(Long vendorId) {
        return vendorRepository.findById(vendorId)
                .orElseThrow(() -> new ResourceNotFoundException("Vendor not found with id: " + vendorId));
    }
}
