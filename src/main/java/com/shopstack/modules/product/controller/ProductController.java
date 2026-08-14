package com.shopstack.modules.product.controller;

import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.product.dto.requests.CreateProductRequest;
import com.shopstack.modules.product.dto.requests.UpdateProductRequest;
import com.shopstack.modules.product.dto.responses.ProductResponse;
import com.shopstack.modules.product.service.ProductService;
import com.shopstack.modules.vendor.entity.Vendor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.MediaType;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final SecurityUtil securityUtil;

    // ---- Vendor: add / edit / remove own products ----
    // A VENDOR always acts on their own vendor profile; an ADMIN may manage any product.

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ProductResponse createProduct(@Valid @ModelAttribute CreateProductRequest request) {

        if (!securityUtil.isCurrentUserAdmin()) {
            Vendor vendor = securityUtil.getCurrentVendor();
            request.setVendorId(vendor.getVendorId());
        }
        return productService.createProduct(request);
    }

    @PutMapping(value="/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ProductResponse updateProduct(
            @PathVariable UUID id,
            @Valid @ModelAttribute UpdateProductRequest request) {

        if (securityUtil.isCurrentUserAdmin()) {
            return productService.updateProduct(id, request);
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        return productService.updateProduct(id, request, vendor.getVendorId());
    }

    @PutMapping(value="/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public ProductResponse updateProductJson(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateProductRequest request) {

        if (securityUtil.isCurrentUserAdmin()) {
            return productService.updateProduct(id, request);
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        return productService.updateProduct(id, request, vendor.getVendorId());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public void deleteProduct(@PathVariable UUID id) {

        if (securityUtil.isCurrentUserAdmin()) {
            productService.deleteProduct(id);
            return;
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        productService.deleteProduct(id, vendor.getVendorId());
    }

    // A vendor's own product list (includes PENDING / REJECTED items), or admin lookup by vendor id.
    @GetMapping("/my-products")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public List<ProductResponse> getMyProducts() {
        Vendor vendor = securityUtil.getCurrentVendor();
        return productService.getProductsByVendor(vendor.getVendorId());
    }

    @GetMapping("/vendor/{vendorId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public List<ProductResponse> getProductsByVendor(@PathVariable Long vendorId) {
        return productService.getProductsByVendor(vendorId);
    }

    // ---- Admin: full, unfiltered catalog (incl. pending/rejected) ----

    @GetMapping
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public List<ProductResponse> getAllProducts() {
        return productService.getAllProducts();
    }

    // ---- Public / customer: browsing only ever shows approved + active products ----

    @GetMapping("/{id}")
    public ProductResponse getProduct(@PathVariable UUID id) {
        return productService.getProductById(id);
    }

    @GetMapping("/browse")
    public List<ProductResponse> browseProducts() {
        return productService.getActiveProducts();
    }

    @GetMapping("/search")
    public List<ProductResponse> searchProducts(@RequestParam String keyword) {
        return productService.searchProducts(keyword);
    }

    @GetMapping("/category/{categoryId}")
    public List<ProductResponse> getProductsByCategory(@PathVariable UUID categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    @GetMapping("/brand/{brand}")
    public List<ProductResponse> getProductsByBrand(@PathVariable String brand) {
        return productService.getProductsByBrand(brand);
    }

    @GetMapping("/featured")
    public List<ProductResponse> getFeaturedProducts() {
        return productService.getFeaturedProducts();
    }

    @GetMapping("/active")
    public List<ProductResponse> getActiveProducts() {
        return productService.getActiveProducts();
    }

    @GetMapping("/price")
    public List<ProductResponse> getProductsByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return productService.getProductsByPriceRange(min, max);
    }
}
