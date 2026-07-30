package com.shopstack.modules.user.repository;

import com.shopstack.modules.user.entity.Address;
import com.shopstack.modules.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface AddressRepository extends JpaRepository<Address, UUID>, JpaSpecificationExecutor<Address> {
    List<Address> findByUser(User user);
}