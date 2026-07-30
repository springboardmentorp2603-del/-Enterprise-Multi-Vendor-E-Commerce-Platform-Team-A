package com.shopstack.modules.product.service;

import com.shopstack.modules.product.dto.requests.CreateProductRequest;
import com.shopstack.modules.product.dto.requests.UpdateProductRequest;
import com.shopstack.modules.product.dto.responses.ProductResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse updateProduct(UUID id, UpdateProductRequest request);

    /** Vendor-scoped update: throws ForbiddenException if actingVendorId doesn't own the product. */
    ProductResponse updateProduct(UUID id, UpdateProductRequest request, Long actingVendorId);

    void deleteProduct(UUID id);

    /** Vendor-scoped delete: throws ForbiddenException if actingVendorId doesn't own the product. */
    void deleteProduct(UUID id, Long actingVendorId);

    ProductResponse getProductById(UUID id);

    List<ProductResponse> getAllProducts();

    List<ProductResponse> searchProducts(String keyword);

    List<ProductResponse> getProductsByCategory(UUID categoryId);

    List<ProductResponse> getProductsByBrand(String brand);

    List<ProductResponse> getProductsByVendor(Long vendorId);

    List<ProductResponse> getProductsByPriceRange(BigDecimal minPrice,
                                                  BigDecimal maxPrice);

    List<ProductResponse> getFeaturedProducts();

    /** Customer-facing catalog: active AND admin-approved products only. */
    List<ProductResponse> getActiveProducts();

    // ---- Admin approval workflow ----

    List<ProductResponse> getPendingProducts();

    ProductResponse approveProduct(UUID id, UUID reviewerUserId, String remarks);

    ProductResponse rejectProduct(UUID id, UUID reviewerUserId, String remarks);
}