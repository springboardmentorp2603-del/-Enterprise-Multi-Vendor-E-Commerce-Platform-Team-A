package com.shopstack.modules.customer.service;

import com.shopstack.common.enums.AccountStatus;
import com.shopstack.common.enums.Gender;
import com.shopstack.common.exception.ConflictException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.customer.dto.requests.CustomerAddressRequest;
import com.shopstack.modules.customer.dto.requests.CustomerRegistrationRequest;
import com.shopstack.modules.customer.dto.requests.CustomerUpdateRequest;
import com.shopstack.modules.customer.dto.responses.CustomerAddressResponse;
import com.shopstack.modules.customer.dto.responses.CustomerResponse;
import com.shopstack.modules.customer.entity.Customer;
import com.shopstack.modules.customer.entity.CustomerAddress;
import com.shopstack.modules.customer.mapper.CustomerMapper;
import com.shopstack.modules.customer.repository.CustomerAddressRepository;
import com.shopstack.modules.customer.repository.CustomerRepository;
import com.shopstack.modules.user.entity.Role;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.repository.RoleRepository;
import com.shopstack.modules.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DefaultCustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository customerAddressRepository;
    private final CustomerMapper customerMapper;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public DefaultCustomerServiceImpl(CustomerRepository customerRepository,
                                       CustomerAddressRepository customerAddressRepository,
                                       CustomerMapper customerMapper,
                                       UserRepository userRepository,
                                       RoleRepository roleRepository,
                                       PasswordEncoder passwordEncoder) {
        this.customerRepository = customerRepository;
        this.customerAddressRepository = customerAddressRepository;
        this.customerMapper = customerMapper;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public CustomerResponse registerCustomer(CustomerRegistrationRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("A customer with this email already exists");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("An account with this email already exists");
        }

        Role customerRole = roleRepository.findByName("CUSTOMER")
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: CUSTOMER"));

        String[] nameParts = request.getFullName() != null
                ? request.getFullName().trim().split("\\s+", 2)
                : new String[]{"", ""};

        Long phoneAsLong = parsePhone(request.getPhoneNumber());

        // Create the login account (this is what /auth/login authenticates against).
        User user = User.builder()
                .firstName(nameParts[0])
                .lastName(nameParts.length > 1 ? nameParts[1] : "")
                .name(request.getFullName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .phone(phoneAsLong)
                .gender(Gender.OTHER)
                .status(AccountStatus.ACTIVE)
                .enabled(true)
                .emailVerified(false)
                .phoneVerified(false)
                .role(customerRole)
                .build();
        User savedUser = userRepository.save(user);

        // Create the customer profile that stores marketplace-specific details/addresses.
        Customer customer = Customer.builder()
                .user(savedUser)
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .active(true)
                .build();

        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    private Long parsePhone(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(phoneNumber.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerById(UUID id) {
        Customer customer = findCustomerOrThrow(id);
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse getCustomerByUserId(UUID userId) {
        Customer customer = customerRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No customer profile found for user id: " + userId));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAllCustomers() {
        return customerRepository.findAll()
                .stream()
                .map(customerMapper::toResponse)
                .toList();
    }

    @Override
    public CustomerResponse updateCustomer(UUID id, CustomerUpdateRequest request) {
        Customer customer = findCustomerOrThrow(id);

        if (request.getFullName() != null) {
            customer.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            customer.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getActive() != null) {
            customer.setActive(request.getActive());
        }

        Customer saved = customerRepository.save(customer);
        return customerMapper.toResponse(saved);
    }

    @Override
    public void deleteCustomer(UUID id) {
        Customer customer = findCustomerOrThrow(id);
        customerRepository.delete(customer);
    }

    @Override
    public CustomerAddressResponse addAddress(UUID customerId, CustomerAddressRequest request) {
        Customer customer = findCustomerOrThrow(customerId);

        CustomerAddress address = CustomerAddress.builder()
                .customer(customer)
                .addressLine1(request.getAddressLine1())
                .addressLine2(request.getAddressLine2())
                .city(request.getCity())
                .state(request.getState())
                .postalCode(request.getPostalCode())
                .country(request.getCountry())
                .isDefault(request.isDefault())
                .build();

        CustomerAddress saved = customerAddressRepository.save(address);
        return customerMapper.toAddressResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> getAddresses(UUID customerId) {
        findCustomerOrThrow(customerId);
        List<CustomerAddress> addresses = customerAddressRepository.findByCustomerId(customerId);
        return customerMapper.toAddressResponseList(addresses);
    }

    private Customer findCustomerOrThrow(UUID id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found with id: " + id));
    }
}
