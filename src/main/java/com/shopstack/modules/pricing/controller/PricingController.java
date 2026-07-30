package com.shopstack.modules.pricing.controller;

import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.pricing.dto.requests.BulkPriceUpdateRequest;
import com.shopstack.modules.pricing.dto.requests.CreateDiscountRequest;
import com.shopstack.modules.pricing.dto.requests.UpdateDiscountRequest;
import com.shopstack.modules.pricing.dto.requests.UpdatePriceRequest;
import com.shopstack.modules.pricing.dto.responses.DiscountResponse;
import com.shopstack.modules.pricing.dto.responses.EffectivePriceResponse;
import com.shopstack.modules.pricing.dto.responses.PriceHistoryResponse;
import com.shopstack.modules.pricing.service.PricingService;
import com.shopstack.modules.vendor.entity.Vendor;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;
    private final SecurityUtil securityUtil;

    // ---- Public: needed by product listing pages and Cart/Checkout ----

    @GetMapping("/{productId}/effective-price")
    public EffectivePriceResponse getEffectivePrice(@PathVariable UUID productId) {
        return pricingService.calculateEffectivePrice(productId);
    }

    @PostMapping("/effective-prices")
    public List<EffectivePriceResponse> getEffectivePrices(@RequestBody List<UUID> productIds) {
        return pricingService.calculateEffectivePrices(productIds);
    }

    @GetMapping("/{productId}/discounts")
    public List<DiscountResponse> getActiveDiscountsForProduct(@PathVariable UUID productId) {
        return pricingService.getActiveDiscountsForProduct(productId);
    }

    // ---- Vendor/Admin: price management ----

    @PutMapping("/{productId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public PriceHistoryResponse setPrice(@PathVariable UUID productId, @Valid @RequestBody UpdatePriceRequest request) {
        UUID performedBy = securityUtil.getCurrentUser().getId();

        if (securityUtil.isCurrentUserAdmin()) {
            return pricingService.setBasePrice(productId, request.getPrice(), performedBy, request.getNote());
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        return pricingService.setBasePrice(productId, request.getPrice(), performedBy,
                request.getNote(), vendor.getVendorId());
    }

    @PostMapping("/bulk-update")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public List<PriceHistoryResponse> bulkUpdatePrices(@Valid @RequestBody BulkPriceUpdateRequest request) {
        UUID performedBy = securityUtil.getCurrentUser().getId();
        Long vendorId = securityUtil.isCurrentUserAdmin() ? null : securityUtil.getCurrentVendor().getVendorId();
        return pricingService.bulkUpdatePrices(request, performedBy, vendorId);
    }

    // NOTE: not yet ownership-scoped — any authenticated vendor/admin can
    // view any product's price history. Tighten with a vendor-overload on
    // getPriceHistory (mirroring Inventory's getHistory(id, vendorId))
    // before this ships to production.
    @GetMapping("/{productId}/history")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public List<PriceHistoryResponse> getPriceHistory(@PathVariable UUID productId) {
        return pricingService.getPriceHistory(productId);
    }

    // ---- Vendor/Admin: discounts ----

    @PostMapping("/discounts")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public DiscountResponse createDiscount(@Valid @RequestBody CreateDiscountRequest request) {
        if (securityUtil.isCurrentUserAdmin()) {
            return pricingService.createDiscount(request);
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        return pricingService.createDiscount(request, vendor.getVendorId());
    }

    @PutMapping("/discounts/{discountId}")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public DiscountResponse updateDiscount(@PathVariable UUID discountId, @Valid @RequestBody UpdateDiscountRequest request) {
        if (securityUtil.isCurrentUserAdmin()) {
            return pricingService.updateDiscount(discountId, request);
        }
        Vendor vendor = securityUtil.getCurrentVendor();
        return pricingService.updateDiscount(discountId, request, vendor.getVendorId());
    }

    @PostMapping("/discounts/{discountId}/deactivate")
    @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    public void deactivateDiscount(@PathVariable UUID discountId) {
        if (securityUtil.isCurrentUserAdmin()) {
            pricingService.deactivateDiscount(discountId);
        } else {
            Vendor vendor = securityUtil.getCurrentVendor();
            pricingService.deactivateDiscount(discountId, vendor.getVendorId());
        }
    }

    @GetMapping("/discounts/my-discounts")
    @PreAuthorize("hasRole('VENDOR')")
    public List<DiscountResponse> getMyDiscounts() {
        Vendor vendor = securityUtil.getCurrentVendor();
        return pricingService.getDiscountsByVendor(vendor.getVendorId());
    }
}