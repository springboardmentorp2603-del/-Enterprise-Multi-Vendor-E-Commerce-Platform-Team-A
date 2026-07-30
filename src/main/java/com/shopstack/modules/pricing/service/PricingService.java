package com.shopstack.modules.pricing.service;

import com.shopstack.modules.pricing.dto.requests.BulkPriceUpdateRequest;
import com.shopstack.modules.pricing.dto.requests.CreateDiscountRequest;
import com.shopstack.modules.pricing.dto.requests.UpdateDiscountRequest;
import com.shopstack.modules.pricing.dto.responses.DiscountResponse;
import com.shopstack.modules.pricing.dto.responses.EffectivePriceResponse;
import com.shopstack.modules.pricing.dto.responses.PriceHistoryResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PricingService {

    // ---- Price management ----

    BigDecimal getCurrentPrice(UUID productId);

    PriceHistoryResponse setBasePrice(UUID productId, BigDecimal price, UUID performedBy, String note);
    PriceHistoryResponse setBasePrice(UUID productId, BigDecimal price, UUID performedBy, String note, Long vendorId);

    List<PriceHistoryResponse> bulkUpdatePrices(BulkPriceUpdateRequest request, UUID performedBy, Long vendorId);

    List<PriceHistoryResponse> getPriceHistory(UUID productId);

    // ---- Discounts ----
    // CATEGORY-scoped discounts affect every vendor's products in that
    // category, so only the admin overload may create/update one.
    // The vendor overload rejects CATEGORY scope and enforces product ownership.

    DiscountResponse createDiscount(CreateDiscountRequest request); // admin
    DiscountResponse createDiscount(CreateDiscountRequest request, Long vendorId); // vendor

    DiscountResponse updateDiscount(UUID discountId, UpdateDiscountRequest request); // admin
    DiscountResponse updateDiscount(UUID discountId, UpdateDiscountRequest request, Long vendorId); // vendor

    void deactivateDiscount(UUID discountId); // admin
    void deactivateDiscount(UUID discountId, Long vendorId); // vendor

    List<DiscountResponse> getDiscountsByVendor(Long vendorId);
    List<DiscountResponse> getActiveDiscountsForProduct(UUID productId);

    // ---- Dynamic pricing — what Cart/Checkout actually call ----

    EffectivePriceResponse calculateEffectivePrice(UUID productId);

    List<EffectivePriceResponse> calculateEffectivePrices(List<UUID> productIds);
}