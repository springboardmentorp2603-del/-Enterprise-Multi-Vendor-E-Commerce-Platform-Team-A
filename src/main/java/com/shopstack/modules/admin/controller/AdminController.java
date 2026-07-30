package com.shopstack.modules.admin.controller;

import com.shopstack.common.enums.VendorStatus;
import com.shopstack.common.response.ApiResponse;
import com.shopstack.common.response.ApiResponseBuilder;
import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.admin.dto.requests.AdminReviewRequest;
import com.shopstack.modules.admin.dto.responses.AdminDashboardStatsResponse;
import com.shopstack.modules.admin.service.AdminDashboardService;
import com.shopstack.modules.auth.dto.RegisterRequest;
import com.shopstack.modules.auth.dto.UserResponse;
import com.shopstack.modules.auth.service.AuthService;
import com.shopstack.modules.customer.dto.responses.CustomerResponse;
import com.shopstack.modules.customer.service.CustomerService;
import com.shopstack.modules.product.dto.requests.CreateCategoryRequest;
import com.shopstack.modules.product.dto.requests.UpdateCategoryRequest;
import com.shopstack.modules.product.dto.responses.CategoryResponse;
import com.shopstack.modules.product.dto.responses.ProductResponse;
import com.shopstack.modules.product.service.CategoryService;
import com.shopstack.modules.product.service.ProductService;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.vendor.dto.requests.VendorApprovalRequest;
import com.shopstack.modules.vendor.dto.responses.VendorApprovalLogResponse;
import com.shopstack.modules.vendor.dto.responses.VendorResponse;
import com.shopstack.modules.vendor.service.VendorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * Single authoritative surface for administrator actions:
 *  - vendor onboarding approval / rejection
 *  - product listing approval / rejection
 *  - category (taxonomy) management
 *  - read-only oversight of the customer base
 *
 * Every endpoint here is also covered by the "/admin/**" -> hasRole('ADMIN') rule in
 * SecurityConfig; the class-level @PreAuthorize is defense in depth.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final VendorService vendorService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final CustomerService customerService;
    private final SecurityUtil securityUtil;
    private final AuthService authService;
    private final AdminDashboardService dashboardService;

    // =====================================================================
    // Dashboard summary
    // =====================================================================

    @GetMapping("/dashboard/stats")
    public ApiResponse<AdminDashboardStatsResponse> getDashboardStats() {
        return ApiResponseBuilder.success("Dashboard stats fetched successfully", dashboardService.getStats());
    }

    // =====================================================================
    // Vendor approval workflow
    // =====================================================================

    @GetMapping("/vendors")
    public ApiResponse<Page<VendorResponse>> listVendors(
            @RequestParam(required = false) VendorStatus status,
            Pageable pageable) {
        return ApiResponseBuilder.success("Vendors fetched successfully",
                vendorService.listVendors(status, pageable));
    }

    @GetMapping("/vendors/pending")
    public ApiResponse<Page<VendorResponse>> listPendingVendors(Pageable pageable) {
        return ApiResponseBuilder.success("Pending vendors fetched successfully",
                vendorService.listVendors(VendorStatus.PENDING, pageable));
    }

    @GetMapping("/vendors/{vendorId}")
    public ApiResponse<VendorResponse> getVendor(@PathVariable Long vendorId) {
        return ApiResponseBuilder.success("Vendor fetched successfully",
                vendorService.getVendorById(vendorId));
    }

    @PostMapping("/vendors/{vendorId}/approve")
    public ApiResponse<VendorResponse> approveVendor(@PathVariable Long vendorId,
                                                       @RequestBody(required = false) AdminReviewRequest request) {
        return reviewVendor(vendorId, VendorStatus.ACTIVE, request, "Vendor approved successfully");
    }

    @PostMapping("/vendors/{vendorId}/reject")
    public ApiResponse<VendorResponse> rejectVendor(@PathVariable Long vendorId,
                                                      @RequestBody(required = false) AdminReviewRequest request) {
        return reviewVendor(vendorId, VendorStatus.REJECTED, request, "Vendor rejected successfully");
    }

    @PostMapping("/vendors/{vendorId}/suspend")
    public ApiResponse<VendorResponse> suspendVendor(@PathVariable Long vendorId,
                                                       @RequestBody(required = false) AdminReviewRequest request) {
        return reviewVendor(vendorId, VendorStatus.SUSPENDED, request, "Vendor suspended successfully");
    }

    @GetMapping("/vendors/{vendorId}/approval-history")
    public ApiResponse<List<VendorApprovalLogResponse>> getVendorApprovalHistory(@PathVariable Long vendorId) {
        return ApiResponseBuilder.success("Approval history fetched successfully",
                vendorService.getApprovalHistory(vendorId));
    }

    private ApiResponse<VendorResponse> reviewVendor(Long vendorId, VendorStatus newStatus,
                                                       AdminReviewRequest request, String successMessage) {
        User admin = securityUtil.getCurrentUser();
        String remarks = request != null ? request.getRemarks() : null;

        VendorApprovalRequest approvalRequest = VendorApprovalRequest.builder()
                .vendorId(vendorId)
                .newStatus(newStatus)
                .reviewedByUserId(admin.getId())
                .remarks(remarks)
                .build();

        return ApiResponseBuilder.success(successMessage, vendorService.reviewVendor(approvalRequest));
    }

    // =====================================================================
    // Product approval workflow
    // =====================================================================

    @GetMapping("/products")
    public ApiResponse<List<ProductResponse>> listAllProducts() {
        return ApiResponseBuilder.success("Products fetched successfully", productService.getAllProducts());
    }

    @GetMapping("/products/pending")
    public ApiResponse<List<ProductResponse>> listPendingProducts() {
        return ApiResponseBuilder.success("Pending products fetched successfully", productService.getPendingProducts());
    }

    @GetMapping("/products/{id}")
    public ApiResponse<ProductResponse> getProduct(@PathVariable UUID id) {
        return ApiResponseBuilder.success("Product fetched successfully", productService.getProductById(id));
    }

    @PostMapping("/products/{id}/approve")
    public ApiResponse<ProductResponse> approveProduct(@PathVariable UUID id,
                                                         @RequestBody(required = false) AdminReviewRequest request) {
        User admin = securityUtil.getCurrentUser();
        String remarks = request != null ? request.getRemarks() : null;
        return ApiResponseBuilder.success("Product approved successfully",
                productService.approveProduct(id, admin.getId(), remarks));
    }

    @PostMapping("/products/{id}/reject")
    public ApiResponse<ProductResponse> rejectProduct(@PathVariable UUID id,
                                                        @RequestBody(required = false) AdminReviewRequest request) {
        User admin = securityUtil.getCurrentUser();
        String remarks = request != null ? request.getRemarks() : null;
        return ApiResponseBuilder.success("Product rejected successfully",
                productService.rejectProduct(id, admin.getId(), remarks));
    }

    // =====================================================================
    // Category (taxonomy) management
    // =====================================================================

    @GetMapping("/categories")
    public ApiResponse<List<CategoryResponse>> listCategories() {
        return ApiResponseBuilder.success("Categories fetched successfully", categoryService.getAllCategories());
    }

    @PostMapping("/categories")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CategoryResponse> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        return ApiResponseBuilder.success("Category created successfully", categoryService.createCategory(request));
    }

    @PutMapping("/categories/{id}")
    public ApiResponse<CategoryResponse> updateCategory(@PathVariable UUID id,
                                                          @Valid @RequestBody UpdateCategoryRequest request) {
        return ApiResponseBuilder.success("Category updated successfully", categoryService.updateCategory(id, request));
    }

    @DeleteMapping("/categories/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.deleteCategory(id);
        return ApiResponseBuilder.success("Category deleted successfully");
    }

    // =====================================================================
    // Staff account provisioning (ADMIN / WAREHOUSE_STAFF cannot self-register)
    // =====================================================================

    @PostMapping("/staff")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<UserResponse> createStaffAccount(@Valid @RequestBody RegisterRequest request) {
        return ApiResponseBuilder.success("Staff account created successfully",
                new UserResponse(authService.registerPrivileged(request)));
    }

    // =====================================================================
    // Customer oversight (read-only)
    // =====================================================================

    @GetMapping("/customers")
    public ApiResponse<List<CustomerResponse>> listCustomers() {
        return ApiResponseBuilder.success("Customers fetched successfully", customerService.getAllCustomers());
    }

    @GetMapping("/customers/{id}")
    public ApiResponse<CustomerResponse> getCustomer(@PathVariable UUID id) {
        return ApiResponseBuilder.success("Customer fetched successfully", customerService.getCustomerById(id));
    }
}
