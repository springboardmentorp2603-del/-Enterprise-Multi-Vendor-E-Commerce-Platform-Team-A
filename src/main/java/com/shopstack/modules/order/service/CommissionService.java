package com.shopstack.modules.order.service;

import com.shopstack.modules.order.entity.CommissionLedger;
import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.entity.OrderItem;
import com.shopstack.modules.order.enums.LedgerStatus;
import com.shopstack.modules.order.enums.LedgerTransactionType;
import com.shopstack.modules.order.repository.CommissionLedgerRepository;
import com.shopstack.modules.product.entity.CategoryCommissionRate;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.CategoryCommissionRateRepository;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.vendor.entity.Vendor;
import com.shopstack.modules.vendor.repository.VendorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CommissionService {

    private final CategoryCommissionRateRepository categoryCommissionRateRepository;
    private final CommissionLedgerRepository commissionLedgerRepository;
    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;

    /**
     * Determines the applicable commission rate.
     * Priority: Active Category Rate Override -> Vendor Default Fallback -> 0%
     */
    public BigDecimal getApplicableCommissionRate(UUID productId, UUID categoryId, Long vendorId, LocalDateTime orderDate) {
        if (categoryId != null) {
            List<CategoryCommissionRate> activeRates = categoryCommissionRateRepository.findActiveRateAtDate(categoryId, orderDate);
            if (!activeRates.isEmpty()) {
                BigDecimal rate = activeRates.get(0).getCommissionRate();
                log.info("Applying category-specific commission rate of {}% for product {}", rate, productId);
                return rate;
            }
        }

        if (vendorId != null) {
            Optional<Vendor> vendorOpt = vendorRepository.findById(vendorId);
            if (vendorOpt.isPresent() && vendorOpt.get().getCommissionRate() != null) {
                BigDecimal rate = vendorOpt.get().getCommissionRate();
                log.info("Applying vendor default commission rate of {}% for product {}", rate, productId);
                return rate;
            }
        }

        log.warn("No active category or vendor default rate found for product {}. Applying 0%.", productId);
        return BigDecimal.ZERO;
    }

    /**
     * Calculates commission using HALF_UP rounding mode (currency scale = 2).
     */
    public BigDecimal calculateCommission(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calculates vendor earnings.
     */
    public BigDecimal calculateVendorEarning(BigDecimal amount, BigDecimal commission) {
        return amount.subtract(commission).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Creates COMMISSION ledger entries for all eligible items in the order.
     */
    public List<CommissionLedger> createCommissionLedger(Order order) {
        if (order == null || order.getItems() == null) {
            return Collections.emptyList();
        }

        List<CommissionLedger> createdEntries = new ArrayList<>();
        LocalDateTime orderDate = order.getCreatedAt() != null ? order.getCreatedAt() : LocalDateTime.now();

        for (OrderItem item : order.getItems()) {
            // Idempotency check: prevent duplicate commission creation for this order item
            if (commissionLedgerRepository.existsByOrderItemIdAndTransactionType(item.getId(), LedgerTransactionType.COMMISSION)) {
                log.warn("COMMISSION ledger entry already exists for order item {}. Skipping.", item.getId());
                continue;
            }

            Product product = item.getProduct();
            if (product == null && item.getProductId() != null) {
                try {
                    product = productRepository.findById(UUID.fromString(item.getProductId())).orElse(null);
                } catch (Exception ignored) {}
            }

            if (product == null) {
                log.error("Product with ID {} not found for order item {}. Cannot calculate commission.", item.getProductId(), item.getId());
                continue;
            }

            UUID categoryId = product.getCategory() != null ? product.getCategory().getId() : null;
            Long vendorId = product.getVendorId();

            BigDecimal rate = getApplicableCommissionRate(product.getId(), categoryId, vendorId, orderDate);
            BigDecimal gross = BigDecimal.valueOf(item.getPrice()).multiply(BigDecimal.valueOf(item.getQuantity())).setScale(2, RoundingMode.HALF_UP);
            BigDecimal commissionAmount = calculateCommission(gross, rate);
            BigDecimal vendorAmount = calculateVendorEarning(gross, commissionAmount);

            CommissionLedger ledger = CommissionLedger.builder()
                    .vendorId(vendorId)
                    .orderId(order.getId())
                    .orderItemId(item.getId())
                    .productId(product.getId())
                    .categoryId(categoryId)
                    .commissionRate(rate)
                    .grossAmount(gross)
                    .commissionAmount(commissionAmount)
                    .vendorAmount(vendorAmount)
                    .transactionType(LedgerTransactionType.COMMISSION)
                    .status(LedgerStatus.CONFIRMED)
                    .description("Confirmed sales commission for Order Item " + item.getId())
                    .build();

            createdEntries.add(commissionLedgerRepository.save(ledger));
        }

        return createdEntries;
    }

    /**
     * Creates REFUND_REVERSAL entries for all items in the order.
     */
    public void createRefundReversalForOrder(UUID orderId) {
        List<CommissionLedger> originals = commissionLedgerRepository.findByOrderId(orderId);
        for (CommissionLedger original : originals) {
            if (original.getTransactionType() == LedgerTransactionType.COMMISSION && original.getStatus() == LedgerStatus.CONFIRMED) {
                try {
                    createRefundReversal(original.getOrderItemId(), original.getGrossAmount());
                } catch (Exception e) {
                    log.error("Failed to reverse commission for order item {}: {}", original.getOrderItemId(), e.getMessage());
                }
            }
        }
    }

    /**
     * Creates a REFUND_REVERSAL ledger entry for a specific order item.
     */
    public CommissionLedger createRefundReversal(UUID orderItemId, BigDecimal refundAmount) {
        List<CommissionLedger> entries = commissionLedgerRepository.findByOrderItemId(orderItemId);
        
        CommissionLedger original = entries.stream()
                .filter(e -> e.getTransactionType() == LedgerTransactionType.COMMISSION)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Original commission ledger entry not found for order item: " + orderItemId));

        // Aggregate existing reversals
        BigDecimal reversedGross = BigDecimal.ZERO;
        BigDecimal reversedComm = BigDecimal.ZERO;
        BigDecimal reversedVendor = BigDecimal.ZERO;

        for (CommissionLedger entry : entries) {
            if (entry.getTransactionType() == LedgerTransactionType.REFUND_REVERSAL && entry.getStatus() == LedgerStatus.CONFIRMED) {
                reversedGross = reversedGross.add(entry.getGrossAmount().abs());
                reversedComm = reversedComm.add(entry.getCommissionAmount().abs());
                reversedVendor = reversedVendor.add(entry.getVendorAmount().abs());
            }
        }

        BigDecimal remainingGross = original.getGrossAmount().subtract(reversedGross);
        if (refundAmount.compareTo(remainingGross) > 0) {
            throw new IllegalArgumentException("Refund amount " + refundAmount + " exceeds remaining commission-eligible gross amount " + remainingGross);
        }

        BigDecimal reversalGross = refundAmount;
        BigDecimal reversalComm = calculateCommission(reversalGross, original.getCommissionRate());

        // Cap commission and vendor amount to remaining balances to avoid fractional rounding errors
        BigDecimal remainingComm = original.getCommissionAmount().subtract(reversedComm);
        if (reversalComm.compareTo(remainingComm) > 0) {
            reversalComm = remainingComm;
        }

        BigDecimal remainingVendor = original.getVendorAmount().subtract(reversedVendor);
        BigDecimal reversalVendor = reversalGross.subtract(reversalComm).setScale(2, RoundingMode.HALF_UP);
        if (reversalVendor.compareTo(remainingVendor) > 0) {
            reversalVendor = remainingVendor;
        }

        CommissionLedger reversal = CommissionLedger.builder()
                .vendorId(original.getVendorId())
                .orderId(original.getOrderId())
                .orderItemId(original.getOrderItemId())
                .productId(original.getProductId())
                .categoryId(original.getCategoryId())
                .commissionRate(original.getCommissionRate())
                .grossAmount(reversalGross.negate())
                .commissionAmount(reversalComm.negate())
                .vendorAmount(reversalVendor.negate())
                .transactionType(LedgerTransactionType.REFUND_REVERSAL)
                .status(LedgerStatus.CONFIRMED)
                .referenceLedgerId(original.getId())
                .description("Refund reversal of Rs. " + refundAmount + " for Order Item " + orderItemId)
                .build();

        CommissionLedger savedReversal = commissionLedgerRepository.save(reversal);

        // If fully reversed, update original status to REVERSED
        if (reversedGross.add(reversalGross).compareTo(original.getGrossAmount()) == 0) {
            original.setStatus(LedgerStatus.REVERSED);
            commissionLedgerRepository.save(original);
        }

        return savedReversal;
    }

    /**
     * Fetches the commission summary details for a vendor.
     */
    public Map<String, Object> getVendorCommissionSummary(Long vendorId) {
        BigDecimal grossSales = commissionLedgerRepository.sumGrossAmountByVendorIdAndType(vendorId, LedgerTransactionType.COMMISSION);
        BigDecimal commissionDeducted = commissionLedgerRepository.sumCommissionAmountByVendorIdAndType(vendorId, LedgerTransactionType.COMMISSION);
        BigDecimal refundReversals = commissionLedgerRepository.sumCommissionAmountByVendorIdAndType(vendorId, LedgerTransactionType.REFUND_REVERSAL);
        BigDecimal netEarnings = commissionLedgerRepository.sumVendorAmountByVendorId(vendorId);
        long totalOrders = commissionLedgerRepository.countOrdersByVendorId(vendorId);

        BigDecimal avgOrderValue = totalOrders > 0
                ? grossSales.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> summary = new HashMap<>();
        summary.put("grossSales", grossSales);
        summary.put("platformCommission", commissionDeducted);
        summary.put("refundReversalAmount", refundReversals.abs()); // Show positive in UI summary card
        summary.put("netVendorEarnings", netEarnings);
        summary.put("totalOrders", totalOrders);
        summary.put("avgOrderValue", avgOrderValue);

        return summary;
    }

    /**
     * Fetches the paginated commission ledger with filters.
     */
    public Page<CommissionLedger> getVendorCommissionLedger(
            Long vendorId,
            LocalDateTime dateFrom,
            LocalDateTime dateTo,
            LedgerTransactionType transactionType,
            String status,
            UUID orderId,
            UUID productId,
            Pageable pageable) {
        return commissionLedgerRepository.findWithFilters(
                vendorId, dateFrom, dateTo, transactionType, status, orderId, productId, pageable);
    }
}
