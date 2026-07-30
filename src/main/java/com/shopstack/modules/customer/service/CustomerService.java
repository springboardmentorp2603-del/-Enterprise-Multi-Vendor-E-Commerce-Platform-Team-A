package com.shopstack.modules.customer.service;

import com.shopstack.modules.customer.dto.requests.CustomerAddressRequest;
import com.shopstack.modules.customer.dto.requests.CustomerRegistrationRequest;
import com.shopstack.modules.customer.dto.requests.CustomerUpdateRequest;
import com.shopstack.modules.customer.dto.responses.CustomerAddressResponse;
import com.shopstack.modules.customer.dto.responses.CustomerResponse;

import java.util.List;
import java.util.UUID;

public interface CustomerService {

    CustomerResponse registerCustomer(CustomerRegistrationRequest request);

    CustomerResponse getCustomerById(UUID id);

    CustomerResponse getCustomerByUserId(UUID userId);

    List<CustomerResponse> getAllCustomers();

    CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request);

    void deleteCustomer(UUID id);

    CustomerAddressResponse addAddress(UUID customerId, CustomerAddressRequest request);

    List<CustomerAddressResponse> getAddresses(UUID customerId);
}
