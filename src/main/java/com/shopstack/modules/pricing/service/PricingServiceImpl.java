package com.shopstack.modules.pricing.service;

import com.shopstack.common.enums.DiscountType;
import com.shopstack.common.enums.ScopeType;
import com.shopstack.common.exception.BadRequestException;
import com.shopstack.common.exception.ForbiddenException;
import com.shopstack.common.exception.ResourceNotFoundException;
import com.shopstack.modules.pricing.dto.requests.BulkPriceUpdateRequest;
import com.shopstack.modules.pricing.dto.requests.CreateDiscountRequest;
import com.shopstack.modules.pricing.dto.requests.UpdateDiscountRequest;
import com.shopstack.modules.pricing.dto.responses.DiscountResponse;
import com.shopstack.modules.pricing.dto.responses.EffectivePriceResponse;
import com.shopstack.modules.pricing.dto.responses.PriceHistoryResponse;
import com.shopstack.modules.pricing.entity.Discount;
import com.shopstack.modules.pricing.entity.PriceHistory;
import com.shopstack.modules.pricing.mapper.PricingMapper;
import com.shopstack.modules.pricing.repository.DiscountRepository;
import com.shopstack.modules.pricing.repository.PriceHistoryRepository;
import com.shopstack.modules.product.entity.Category;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.CategoryRepository;
import com.shopstack.modules.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class PricingServiceImpl implements PricingService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final DiscountRepository discountRepository;
    private final PriceHistoryRepository priceHistoryRepository;
    private final PricingMapper pricingMapper;

    // =================================================================
    // Price management
    // =================================================================

    @Override
    @Transactional(readOnly = true)
    public BigDecimal getCurrentPrice(UUID productId) {
        return findProductOrThrow(productId).getPrice();
    }

    @Override
    public PriceHistoryResponse setBasePrice(UUID productId, BigDecimal price, UUID performedBy, String note) {
        return doSetPrice(findProductOrThrow(productId), price, performedBy, note);
    }

    @Override
    public PriceHistoryResponse setBasePrice(UUID productId, BigDecimal price, UUID performedBy,
                                              String note, Long vendorId) {
        Product product = findProductOrThrow(productId);
        verifyProductOwnership(product, vendorId);
        return doSetPrice(product, price, performedBy, note);
    }

    @Override
    public List<PriceHistoryResponse> bulkUpdatePrices(BulkPriceUpdateRequest request,
                                                        UUID performedBy, Long vendorId) {
        List<UUID> productIds = request.getItems().stream()
                .map(BulkPriceUpdateRequest.Item::getProductId)
                .toList();

        List<Product> products = productRepository.findAllById(productIds);
        if (products.size() != productIds.size()) {
            throw new ResourceNotFoundException("One or more products in the bulk update were not found.");
        }
        if (vendorId != null) {
            for (Product product : products) {
                verifyProductOwnership(product, vendorId);
            }
        }

        return request.getItems().stream()
                .map(item -> {
                    Product product = products.stream()
                            .filter(p -> p.getId().equals(item.getProductId()))
                            .findFirst()
                            .orElseThrow(); // can't happen, already validated above
                    return doSetPrice(product, item.getPrice(), performedBy, "Bulk price update");
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<PriceHistoryResponse> getPriceHistory(UUID productId) {
        return pricingMapper.toPriceHistoryResponseList(
                priceHistoryRepository.findByProductIdOrderByCreatedAtDesc(productId));
    }

    // =================================================================
    // Discounts
    // =================================================================

    @Override
    public DiscountResponse createDiscount(CreateDiscountRequest request) {
        // Admin path: PRODUCT or CATEGORY both allowed, no ownership check.
        return buildAndSaveDiscount(request, null);
    }

    @Override
    public DiscountResponse createDiscount(CreateDiscountRequest request, Long vendorId) {
        if (request.getScopeType() == ScopeType.CATEGORY) {
            throw new BadRequestException(
                    "Vendors cannot create category-wide discounts — those affect every " +
                            "vendor's products in that category. Use a product-scoped discount instead.");
        }
        Product product = findProductOrThrow(request.getProductId());
        verifyProductOwnership(product, vendorId);
        return buildAndSaveDiscount(request, vendorId);
    }

    @Override
    public DiscountResponse updateDiscount(UUID discountId, UpdateDiscountRequest request) {
        return doUpdateDiscount(findDiscountOrThrow(discountId), request);
    }

    @Override
    public DiscountResponse updateDiscount(UUID discountId, UpdateDiscountRequest request, Long vendorId) {
        Discount discount = findDiscountOrThrow(discountId);
        verifyDiscountOwnership(discount, vendorId);
        return doUpdateDiscount(discount, request);
    }

    @Override
    public void deactivateDiscount(UUID discountId) {
        Discount discount = findDiscountOrThrow(discountId);
        discount.setActive(false);
        discountRepository.save(discount);
    }

    @Override
    public void deactivateDiscount(UUID discountId, Long vendorId) {
        Discount discount = findDiscountOrThrow(discountId);
        verifyDiscountOwnership(discount, vendorId);
        discount.setActive(false);
        discountRepository.save(discount);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> getDiscountsByVendor(Long vendorId) {
        return pricingMapper.toDiscountResponseList(discountRepository.findByVendorId(vendorId));
    }

    @Override
    @Transactional(readOnly = true)
    public List<DiscountResponse> getActiveDiscountsForProduct(UUID productId) {
        Product product = findProductOrThrow(productId);
        List<Discount> applicable = discountRepository.findApplicableDiscounts(
                productId, product.getCategory().getId(), LocalDateTime.now());
        return pricingMapper.toDiscountResponseList(applicable);
    }

    // =================================================================
    // Dynamic pricing
    // =================================================================

    @Override
    @Transactional(readOnly = true)
    public EffectivePriceResponse calculateEffectivePrice(UUID productId) {
        Product product = findProductOrThrow(productId);
        BigDecimal basePrice = product.getPrice();

        List<Discount> applicable = discountRepository.findApplicableDiscounts(
                productId, product.getCategory().getId(), LocalDateTime.now());

        Discount best = pickBestDiscount(applicable, basePrice);
        BigDecimal discountAmount = best == null ? BigDecimal.ZERO : computeDiscountAmount(best, basePrice);

        return EffectivePriceResponse.builder()
                .productId(productId)
                .basePrice(basePrice)
                .discountAmount(discountAmount)
                .effectivePrice(basePrice.subtract(discountAmount))
                .appliedDiscountId(best == null ? null : best.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EffectivePriceResponse> calculateEffectivePrices(List<UUID> productIds) {
        // NOTE: one query per product for now (simple and correct). If this
        // becomes a hot path for large listing pages, batch the discount
        // lookup with an IN-clause query instead of calling this per id.
        return productIds.stream().map(this::calculateEffectivePrice).toList();
    }

    // =================================================================
    // Helpers
    // =================================================================

    private PriceHistoryResponse doSetPrice(Product product, BigDecimal newPrice, UUID performedBy, String note) {
        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Price cannot be negative.");
        }

        BigDecimal previous = product.getPrice();
        product.setPrice(newPrice);
        productRepository.save(product);

        PriceHistory history = PriceHistory.builder()
                .productId(product.getId())
                .vendorId(product.getVendorId())
                .oldPrice(previous)
                .newPrice(newPrice)
                .changedBy(performedBy)
                .note(note)
                .build();
        history = priceHistoryRepository.save(history);

        return pricingMapper.toResponse(history);
    }

    private DiscountResponse buildAndSaveDiscount(CreateDiscountRequest request, Long vendorId) {
        Discount.DiscountBuilder builder = Discount.builder()
                .vendorId(vendorId) // null for admin-created CATEGORY discounts
                .scopeType(request.getScopeType())
                .discountType(request.getDiscountType())
                .value(request.getValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .active(true);

        if (request.getScopeType() == ScopeType.PRODUCT) {
            Product product = findProductOrThrow(request.getProductId());
            builder.product(product);
        } else {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
            builder.category(category);
        }

        Discount saved = discountRepository.save(builder.build());
        return pricingMapper.toResponse(saved);
    }

    private DiscountResponse doUpdateDiscount(Discount discount, UpdateDiscountRequest request) {
        if (request.getValue() != null) {
            if (discount.getDiscountType() == DiscountType.PERCENTAGE
                    && request.getValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new BadRequestException("Percentage discount value cannot exceed 100.");
            }
            discount.setValue(request.getValue());
        }
        if (request.getStartDate() != null) {
            discount.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            discount.setEndDate(request.getEndDate());
        }
        if (discount.getEndDate() != null && discount.getStartDate() != null
                && !discount.getEndDate().isAfter(discount.getStartDate())) {
            throw new BadRequestException("endDate must be after startDate.");
        }
        if (request.getActive() != null) {
            discount.setActive(request.getActive());
        }

        return pricingMapper.toResponse(discountRepository.save(discount));
    }

    /**
     * "Most specific wins": if any PRODUCT-scoped discount applies, only
     * those are considered, ignoring CATEGORY-scoped ones entirely. Among
     * remaining candidates (ties in specificity), the one giving the
     * customer the larger discount amount wins.
     */
    private Discount pickBestDiscount(List<Discount> discounts, BigDecimal basePrice) {
        if (discounts.isEmpty()) {
            return null;
        }
        List<Discount> productScoped = discounts.stream()
                .filter(d -> d.getScopeType() == ScopeType.PRODUCT)
                .toList();
        List<Discount> candidates = productScoped.isEmpty() ? discounts : productScoped;

        return candidates.stream()
                .max(Comparator.comparing(d -> computeDiscountAmount(d, basePrice)))
                .orElse(null);
    }

    private BigDecimal computeDiscountAmount(Discount discount, BigDecimal basePrice) {
        BigDecimal amount = discount.getDiscountType() == DiscountType.PERCENTAGE
                ? basePrice.multiply(discount.getValue())
                        .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                : discount.getValue();

        // A flat discount can never exceed the base price (no negative prices).
        return amount.compareTo(basePrice) > 0 ? basePrice : amount;
    }

    private Product findProductOrThrow(UUID productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    private Discount findDiscountOrThrow(UUID discountId) {
        return discountRepository.findById(discountId)
                .orElseThrow(() -> new ResourceNotFoundException("Discount not found: " + discountId));
    }

    private void verifyProductOwnership(Product product, Long vendorId) {
        if (!product.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("This product does not belong to the current vendor.");
        }
    }

    private void verifyDiscountOwnership(Discount discount, Long vendorId) {
        if (discount.getVendorId() == null || !discount.getVendorId().equals(vendorId)) {
            throw new ForbiddenException("This discount does not belong to the current vendor.");
        }
    }
}