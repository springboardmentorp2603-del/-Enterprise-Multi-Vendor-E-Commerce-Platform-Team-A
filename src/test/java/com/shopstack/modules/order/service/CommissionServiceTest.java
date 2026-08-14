package com.shopstack.modules.order.service;

import com.shopstack.modules.order.entity.CommissionLedger;
import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.entity.OrderItem;
import com.shopstack.modules.order.enums.LedgerStatus;
import com.shopstack.modules.order.enums.LedgerTransactionType;
import com.shopstack.modules.order.repository.CommissionLedgerRepository;
import com.shopstack.modules.product.entity.Category;
import com.shopstack.modules.product.entity.CategoryCommissionRate;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.CategoryCommissionRateRepository;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.vendor.entity.Vendor;
import com.shopstack.modules.vendor.repository.VendorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CommissionServiceTest {

    @Mock
    private CategoryCommissionRateRepository categoryCommissionRateRepository;

    @Mock
    private CommissionLedgerRepository commissionLedgerRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private CommissionService commissionService;

    private UUID categoryId;
    private UUID productId;
    private Long vendorId;
    private LocalDateTime orderDate;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        categoryId = UUID.randomUUID();
        productId = UUID.randomUUID();
        vendorId = 1L;
        orderDate = LocalDateTime.now();
    }

    @Test
    public void testBasicCommissionCalculationVendorFallback() {
        // Vendor default commission = 10%
        Vendor vendor = Vendor.builder()
                .vendorId(vendorId)
                .commissionRate(BigDecimal.valueOf(10))
                .build();

        when(categoryCommissionRateRepository.findActiveRateAtDate(any(), any())).thenReturn(Collections.emptyList());
        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

        // Test fallback lookup
        BigDecimal rate = commissionService.getApplicableCommissionRate(productId, categoryId, vendorId, orderDate);
        assertEquals(0, rate.compareTo(BigDecimal.valueOf(10)));

        // Test math: ₹1,000 item
        BigDecimal gross = BigDecimal.valueOf(1000.00);
        BigDecimal commission = commissionService.calculateCommission(gross, rate);
        BigDecimal vendorEarning = commissionService.calculateVendorEarning(gross, commission);

        assertEquals(0, commission.compareTo(BigDecimal.valueOf(100)));
        assertEquals(0, vendorEarning.compareTo(BigDecimal.valueOf(900)));
    }

    @Test
    public void testCategoryOverride() {
        // Vendor default commission = 10%, Category commission = 8%
        CategoryCommissionRate catRate = CategoryCommissionRate.builder()
                .categoryId(categoryId)
                .commissionRate(BigDecimal.valueOf(8))
                .active(true)
                .effectiveFrom(orderDate.minusDays(5))
                .build();

        when(categoryCommissionRateRepository.findActiveRateAtDate(eq(categoryId), any()))
                .thenReturn(List.of(catRate));

        // Test rate override lookup
        BigDecimal rate = commissionService.getApplicableCommissionRate(productId, categoryId, vendorId, orderDate);
        assertEquals(0, rate.compareTo(BigDecimal.valueOf(8)));

        // Test math: ₹1,000 item
        BigDecimal gross = BigDecimal.valueOf(1000.00);
        BigDecimal commission = commissionService.calculateCommission(gross, rate);
        BigDecimal vendorEarning = commissionService.calculateVendorEarning(gross, commission);

        assertEquals(0, commission.compareTo(BigDecimal.valueOf(80)));
        assertEquals(0, vendorEarning.compareTo(BigDecimal.valueOf(920)));
    }

    @Test
    public void testCategoryFallbackWhenCategoryRateInactive() {
        // No active category rate. Vendor default = 12%
        Vendor vendor = Vendor.builder()
                .vendorId(vendorId)
                .commissionRate(BigDecimal.valueOf(12))
                .build();

        when(categoryCommissionRateRepository.findActiveRateAtDate(any(), any())).thenReturn(Collections.emptyList());
        when(vendorRepository.findById(vendorId)).thenReturn(Optional.of(vendor));

        BigDecimal rate = commissionService.getApplicableCommissionRate(productId, categoryId, vendorId, orderDate);
        assertEquals(0, rate.compareTo(BigDecimal.valueOf(12)));
    }

    @Test
    public void testFullRefundReversal() {
        UUID orderItemId = UUID.randomUUID();
        UUID ledgerId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        CommissionLedger original = CommissionLedger.builder()
                .vendorId(vendorId)
                .orderId(orderId)
                .orderItemId(orderItemId)
                .productId(productId)
                .categoryId(categoryId)
                .commissionRate(BigDecimal.valueOf(10))
                .grossAmount(BigDecimal.valueOf(1000.00))
                .commissionAmount(BigDecimal.valueOf(100.00))
                .vendorAmount(BigDecimal.valueOf(900.00))
                .transactionType(LedgerTransactionType.COMMISSION)
                .status(LedgerStatus.CONFIRMED)
                .build();
        original.setId(ledgerId);

        when(commissionLedgerRepository.findByOrderItemId(orderItemId)).thenReturn(List.of(original));
        when(commissionLedgerRepository.save(any(CommissionLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Full refund: ₹1,000
        CommissionLedger reversal = commissionService.createRefundReversal(orderItemId, BigDecimal.valueOf(1000.00));

        assertNotNull(reversal);
        assertEquals(0, reversal.getGrossAmount().compareTo(BigDecimal.valueOf(-1000.00)));
        assertEquals(0, reversal.getCommissionAmount().compareTo(BigDecimal.valueOf(-100.00)));
        assertEquals(0, reversal.getVendorAmount().compareTo(BigDecimal.valueOf(-900.00)));
        assertEquals(LedgerTransactionType.REFUND_REVERSAL, reversal.getTransactionType());
        assertEquals(original.getId(), reversal.getReferenceLedgerId());
        
        // Assert original status updated to REVERSED
        assertEquals(LedgerStatus.REVERSED, original.getStatus());
    }

    @Test
    public void testPartialRefundReversal() {
        UUID orderItemId = UUID.randomUUID();
        UUID ledgerId = UUID.randomUUID();

        CommissionLedger original = CommissionLedger.builder()
                .vendorId(vendorId)
                .orderId(UUID.randomUUID())
                .orderItemId(orderItemId)
                .productId(productId)
                .categoryId(categoryId)
                .commissionRate(BigDecimal.valueOf(10))
                .grossAmount(BigDecimal.valueOf(1000.00))
                .commissionAmount(BigDecimal.valueOf(100.00))
                .vendorAmount(BigDecimal.valueOf(900.00))
                .transactionType(LedgerTransactionType.COMMISSION)
                .status(LedgerStatus.CONFIRMED)
                .build();
        original.setId(ledgerId);

        when(commissionLedgerRepository.findByOrderItemId(orderItemId)).thenReturn(List.of(original));
        when(commissionLedgerRepository.save(any(CommissionLedger.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Partial refund: ₹400
        CommissionLedger reversal = commissionService.createRefundReversal(orderItemId, BigDecimal.valueOf(400.00));

        assertNotNull(reversal);
        assertEquals(0, reversal.getGrossAmount().compareTo(BigDecimal.valueOf(-400.00)));
        assertEquals(0, reversal.getCommissionAmount().compareTo(BigDecimal.valueOf(-40.00)));
        assertEquals(0, reversal.getVendorAmount().compareTo(BigDecimal.valueOf(-360.00)));
        
        // Original should NOT be marked reversed since it's a partial refund
        assertEquals(LedgerStatus.CONFIRMED, original.getStatus());
    }

    @Test
    public void testMultipleRefundsExcessiveReversalCheck() {
        UUID orderItemId = UUID.randomUUID();
        UUID ledgerId = UUID.randomUUID();

        CommissionLedger original = CommissionLedger.builder()
                .vendorId(vendorId)
                .orderId(UUID.randomUUID())
                .orderItemId(orderItemId)
                .productId(productId)
                .categoryId(categoryId)
                .commissionRate(BigDecimal.valueOf(10))
                .grossAmount(BigDecimal.valueOf(1000.00))
                .commissionAmount(BigDecimal.valueOf(100.00))
                .vendorAmount(BigDecimal.valueOf(900.00))
                .transactionType(LedgerTransactionType.COMMISSION)
                .status(LedgerStatus.CONFIRMED)
                .build();
        original.setId(ledgerId);

        CommissionLedger firstReversal = CommissionLedger.builder()
                .orderItemId(orderItemId)
                .grossAmount(BigDecimal.valueOf(-700.00))
                .commissionAmount(BigDecimal.valueOf(-70.00))
                .vendorAmount(BigDecimal.valueOf(-630.00))
                .transactionType(LedgerTransactionType.REFUND_REVERSAL)
                .status(LedgerStatus.CONFIRMED)
                .build();

        // Already reversed ₹700 (₹300 remaining)
        when(commissionLedgerRepository.findByOrderItemId(orderItemId))
                .thenReturn(List.of(original, firstReversal));

        // Attempting to refund ₹400 should fail because remaining is only ₹300
        assertThrows(IllegalArgumentException.class, () -> {
            commissionService.createRefundReversal(orderItemId, BigDecimal.valueOf(400.00));
        });
    }

    @Test
    public void testCommissionIdempotency() {
        UUID orderItemId = UUID.randomUUID();
        Order order = new Order();
        order.setId(UUID.randomUUID());
        order.setStatus("CONFIRMED");

        OrderItem item = new OrderItem();
        item.setId(orderItemId);
        item.setProductId(productId.toString());
        item.setPrice(1000.0);
        item.setQuantity(1);
        order.setItems(List.of(item));

        // Mock that the ledger entry already exists
        when(commissionLedgerRepository.existsByOrderItemIdAndTransactionType(orderItemId, LedgerTransactionType.COMMISSION))
                .thenReturn(true);

        List<CommissionLedger> created = commissionService.createCommissionLedger(order);
        
        // Assert no new ledger was created (idempotency constraint)
        assertTrue(created.isEmpty());
        verify(commissionLedgerRepository, never()).save(any());
    }
}
