package com.shopstack.modules.customer.controller;

import com.shopstack.common.response.ApiResponse;
import com.shopstack.common.response.ApiResponseBuilder;
import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.customer.dto.requests.CustomerAddressRequest;
import com.shopstack.modules.customer.dto.requests.CustomerRegistrationRequest;
import com.shopstack.modules.customer.dto.requests.CustomerUpdateRequest;
import com.shopstack.modules.customer.dto.responses.CustomerAddressResponse;
import com.shopstack.modules.customer.dto.responses.CustomerResponse;
import com.shopstack.modules.customer.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerService customerService;
    private final SecurityUtil securityUtil;

    public CustomerController(CustomerService customerService, SecurityUtil securityUtil) {
        this.customerService = customerService;
        this.securityUtil = securityUtil;
    }

    // Public sign-up: creates both the login account (User) and the customer profile.
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerResponse> registerCustomer(@Valid @RequestBody CustomerRegistrationRequest request) {
        CustomerResponse response = customerService.registerCustomer(request);
        return ApiResponseBuilder.success("Customer registered successfully", response);
    }

    // The logged-in customer's own profile.
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ApiResponse<CustomerResponse> getMyProfile() {
        UUID userId = securityUtil.getCurrentUser().getId();
        CustomerResponse response = customerService.getCustomerByUserId(userId);
        return ApiResponseBuilder.success("Customer fetched successfully", response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<CustomerResponse> getCustomerById(@PathVariable UUID id) {
        CustomerResponse response = customerService.getCustomerById(id);
        return ApiResponseBuilder.success("Customer fetched successfully", response);
    }

    // Admin oversight only - full customer directory.
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<CustomerResponse>> getAllCustomers() {
        List<CustomerResponse> responses = customerService.getAllCustomers();
        return ApiResponseBuilder.success("Customers fetched successfully", responses);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<CustomerResponse> updateCustomer(@PathVariable UUID id,
                                                          @Valid @RequestBody CustomerUpdateRequest request) {
        CustomerResponse response = customerService.updateCustomer(id, request);
        return ApiResponseBuilder.success("Customer updated successfully", response);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Void> deleteCustomer(@PathVariable UUID id) {
        customerService.deleteCustomer(id);
        return ApiResponseBuilder.success("Customer deleted successfully", null);
    }

    @PostMapping("/{customerId}/addresses")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<CustomerAddressResponse> addAddress(@PathVariable UUID customerId,
                                                             @Valid @RequestBody CustomerAddressRequest request) {
        CustomerAddressResponse response = customerService.addAddress(customerId, request);
        return ApiResponseBuilder.success("Address added successfully", response);
    }

    @GetMapping("/{customerId}/addresses")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ApiResponse<List<CustomerAddressResponse>> getAddresses(@PathVariable UUID customerId) {
        List<CustomerAddressResponse> responses = customerService.getAddresses(customerId);
        return ApiResponseBuilder.success("Addresses fetched successfully", responses);
    }
}
