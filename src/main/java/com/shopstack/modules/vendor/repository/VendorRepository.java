package com.shopstack.modules.vendor.repository;

import com.shopstack.common.enums.VendorStatus;
import com.shopstack.modules.vendor.entity.Vendor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VendorRepository extends JpaRepository<Vendor, Long> {

    Optional<Vendor> findByUser_Id(UUID userId);

    Optional<Vendor> findByBusinessEmail(String businessEmail);

    Optional<Vendor> findByGstNumber(String gstNumber);

    boolean existsByBusinessEmail(String businessEmail);

    boolean existsByGstNumber(String gstNumber);

    Page<Vendor> findByStatus(VendorStatus status, Pageable pageable);

    List<Vendor> findByStatus(VendorStatus status);

    @Query("SELECT v FROM Vendor v WHERE " +
            "LOWER(v.businessName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "OR LOWER(v.businessEmail) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Vendor> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByStatus(VendorStatus status);
}
