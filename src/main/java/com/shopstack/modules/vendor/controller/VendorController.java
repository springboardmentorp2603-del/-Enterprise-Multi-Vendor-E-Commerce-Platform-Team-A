package com.shopstack.modules.vendor.controller;

import com.shopstack.common.enums.VendorStatus;
import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.vendor.dto.requests.*;
import com.shopstack.modules.vendor.dto.responses.VendorApprovalLogResponse;
import com.shopstack.modules.vendor.dto.responses.VendorResponse;
import com.shopstack.modules.vendor.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {

    private final VendorService vendorService;
    private final SecurityUtil securityUtil;

    // ---- Registration & profile ----

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<VendorResponse> registerVendor(@Valid @RequestBody VendorRegistrationRequest request) {
        VendorResponse response = vendorService.registerVendor(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // The logged-in vendor's own profile - lets the frontend resolve vendorId without hardcoding it.
    @GetMapping("/me")
    @PreAuthorize("hasRole('VENDOR')")
    public ResponseEntity<VendorResponse> getMyVendorProfile() {
        UUID userId = securityUtil.getCurrentUser().getId();
        return ResponseEntity.ok(vendorService.getVendorByUserId(userId));
    }

    @GetMapping("/{vendorId}")
    public ResponseEntity<VendorResponse> getVendor(@PathVariable Long vendorId) {
        return ResponseEntity.ok(vendorService.getVendorById(vendorId));
    }

    @GetMapping("/by-user/{userId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<VendorResponse> getVendorByUserId(@PathVariable UUID userId) {
        return ResponseEntity.ok(vendorService.getVendorByUserId(userId));
    }

    // Full vendor directory (incl. PENDING/SUSPENDED/etc.) is admin-only.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<VendorResponse>> listVendors(
            @RequestParam(required = false) VendorStatus status,
            Pageable pageable) {
        return ResponseEntity.ok(vendorService.listVendors(status, pageable));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<VendorResponse>> searchVendors(
            @RequestParam String keyword,
            Pageable pageable) {
        return ResponseEntity.ok(vendorService.searchVendors(keyword, pageable));
    }

    @PatchMapping("/{vendorId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<VendorResponse> updateVendor(
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorUpdateRequest request) {
        return ResponseEntity.ok(vendorService.updateVendor(vendorId, request));
    }

    @DeleteMapping("/{vendorId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVendor(@PathVariable Long vendorId) {
        vendorService.deleteVendor(vendorId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{vendorId}/commission-rate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VendorResponse> updateCommissionRate(
            @PathVariable Long vendorId,
            @RequestBody java.util.Map<String, java.math.BigDecimal> request) {
        java.math.BigDecimal rate = request.get("commissionRate");
        return ResponseEntity.ok(vendorService.updateCommissionRate(vendorId, rate));
    }

    // ---- Addresses, bank details, documents ----

    @PostMapping("/{vendorId}/addresses")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<VendorResponse> addAddress(
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorAddressRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.addAddress(vendorId, request));
    }

    @PostMapping("/{vendorId}/bank-details")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<VendorResponse> addBankDetails(
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorBankDetailsRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.addBankDetails(vendorId, request));
    }

    @PostMapping("/{vendorId}/documents")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ResponseEntity<VendorResponse> uploadDocument(
            @PathVariable Long vendorId,
            @Valid @RequestBody VendorDocumentUploadRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vendorService.uploadDocument(vendorId, request));
    }

    // ---- Admin approval workflow ----

    @PostMapping("/review")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VendorResponse> reviewVendor(@Valid @RequestBody VendorApprovalRequest request) {
        return ResponseEntity.ok(vendorService.reviewVendor(request));
    }

    @GetMapping("/{vendorId}/approval-history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<VendorApprovalLogResponse>> getApprovalHistory(@PathVariable Long vendorId) {
        return ResponseEntity.ok(vendorService.getApprovalHistory(vendorId));
    }
}
