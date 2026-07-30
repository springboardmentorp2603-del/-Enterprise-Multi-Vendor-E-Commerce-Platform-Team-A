package com.shopstack.modules.customer.repository;

import com.shopstack.modules.customer.entity.CustomerAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, UUID>, JpaSpecificationExecutor<CustomerAddress> {

    List<CustomerAddress> findByCustomerId(UUID customerId);
}
