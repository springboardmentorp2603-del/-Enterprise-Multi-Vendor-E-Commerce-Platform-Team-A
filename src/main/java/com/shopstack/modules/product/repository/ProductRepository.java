package com.shopstack.modules.product.repository;

import com.shopstack.common.enums.ProductApprovalStatus;
import com.shopstack.modules.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    List<Product> findByCategory_Id(UUID categoryId);
    List<Product> findByBrand(String brand);
    List<Product> findByVendorId(Long vendorId);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByProductNameContainingIgnoreCase(String keyword);
    List<Product> findByActiveTrue();
    List<Product> findByFeaturedTrue();

    // ---- Approval-workflow / customer-browsing aware queries ----
    List<Product> findByApprovalStatus(ProductApprovalStatus approvalStatus);
    List<Product> findByActiveTrueAndApprovalStatus(ProductApprovalStatus approvalStatus);
    List<Product> findByFeaturedTrueAndActiveTrueAndApprovalStatus(ProductApprovalStatus approvalStatus);
    List<Product> findByCategory_IdAndActiveTrueAndApprovalStatus(UUID categoryId, ProductApprovalStatus approvalStatus);
    List<Product> findByBrandAndActiveTrueAndApprovalStatus(String brand, ProductApprovalStatus approvalStatus);
    List<Product> findByPriceBetweenAndActiveTrueAndApprovalStatus(BigDecimal minPrice, BigDecimal maxPrice, ProductApprovalStatus approvalStatus);
    List<Product> findByProductNameContainingIgnoreCaseAndActiveTrueAndApprovalStatus(String keyword, ProductApprovalStatus approvalStatus);

    long countByApprovalStatus(ProductApprovalStatus approvalStatus);
}