package com.shopstack.modules.vendor.repository;

import com.shopstack.common.enums.AddressType;
import com.shopstack.modules.vendor.entity.VendorAddress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorAddressRepository extends JpaRepository<VendorAddress, Long> {

    List<VendorAddress> findByVendor_VendorId(Long vendorId);

    List<VendorAddress> findByVendor_VendorIdAndAddressType(Long vendorId, AddressType addressType);

    Optional<VendorAddress> findByVendor_VendorIdAndIsDefaultTrue(Long vendorId);

    void deleteByVendor_VendorIdAndAddressId(Long vendorId, Long addressId);
}