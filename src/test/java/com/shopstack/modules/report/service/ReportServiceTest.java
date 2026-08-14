package com.shopstack.modules.report.service;

import com.shopstack.modules.order.entity.CommissionLedger;
import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.entity.OrderItem;
import com.shopstack.modules.order.enums.LedgerStatus;
import com.shopstack.modules.order.enums.LedgerTransactionType;
import com.shopstack.modules.order.repository.CommissionLedgerRepository;
import com.shopstack.modules.order.repository.OrderItemRepository;
import com.shopstack.modules.order.repository.OrderRepository;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.report.dto.responses.VendorEarningsAnalyticsResponse;
import com.shopstack.modules.vendor.entity.Vendor;
import com.shopstack.modules.vendor.repository.VendorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReportServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private VendorRepository vendorRepository;

    @Mock
    private CommissionLedgerRepository commissionLedgerRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ReportService reportService;

    @Test
    void getVendorEarningsAnalyticsReturnsGrossNetAndTopProducts() {
        Vendor vendor = new Vendor();
        vendor.setVendorId(7L);
        vendor.setCommissionRate(new BigDecimal("10"));

        UUID orderId1 = UUID.randomUUID();
        UUID orderId2 = UUID.randomUUID();
        UUID orderItemId1 = UUID.randomUUID();
        UUID orderItemId2 = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        Product product = new Product();
        product.setId(productId);
        product.setVendorId(7L);
        product.setProductName("Widget");

        Order order1 = new Order();
        order1.setId(orderId1);
        order1.setStatus("DELIVERED");
        order1.setCreatedAt(LocalDateTime.now().minusDays(5));

        OrderItem itemOne = new OrderItem();
        itemOne.setId(orderItemId1);
        itemOne.setOrder(order1);
        itemOne.setProduct(product);
        itemOne.setPrice(200.0);
        itemOne.setQuantity(2);

        OrderItem itemTwo = new OrderItem();
        itemTwo.setId(orderItemId2);
        itemTwo.setOrder(order1);
        itemTwo.setProduct(product);
        itemTwo.setPrice(300.0);
        itemTwo.setQuantity(1);

        CommissionLedger ledger1 = CommissionLedger.builder()
                .vendorId(7L)
                .orderId(orderId1)
                .orderItemId(orderItemId1)
                .productId(productId)
                .commissionRate(BigDecimal.valueOf(10))
                .grossAmount(BigDecimal.valueOf(400.00))
                .commissionAmount(BigDecimal.valueOf(40.00))
                .vendorAmount(BigDecimal.valueOf(360.00))
                .transactionType(LedgerTransactionType.COMMISSION)
                .status(LedgerStatus.CONFIRMED)
                .build();
        ledger1.setCreatedAt(LocalDateTime.now().minusDays(5));

        CommissionLedger ledger2 = CommissionLedger.builder()
                .vendorId(7L)
                .orderId(orderId2)
                .orderItemId(orderItemId2)
                .productId(productId)
                .commissionRate(BigDecimal.valueOf(10))
                .grossAmount(BigDecimal.valueOf(300.00))
                .commissionAmount(BigDecimal.valueOf(30.00))
                .vendorAmount(BigDecimal.valueOf(270.00))
                .transactionType(LedgerTransactionType.COMMISSION)
                .status(LedgerStatus.CONFIRMED)
                .build();
        ledger2.setCreatedAt(LocalDateTime.now().minusDays(5));

        when(commissionLedgerRepository.findAllByVendorIdAndRange(eq(7L), any(), any()))
                .thenReturn(List.of(ledger1, ledger2));

        when(orderItemRepository.findAllById(any()))
                .thenReturn(List.of(itemOne, itemTwo));

        when(productRepository.findById(productId))
                .thenReturn(Optional.of(product));

        VendorEarningsAnalyticsResponse response = reportService.getVendorEarningsAnalytics(7L, "30d");

        assertEquals(0, response.getTotalEarnings().compareTo(new BigDecimal("700.00")));
        assertEquals(0, response.getNetPayout().compareTo(new BigDecimal("630.00")));
        assertEquals(0, response.getAvgOrderValue().compareTo(new BigDecimal("350.00"))); // 700 / 2 distinct orders
        assertEquals(2, response.getTotalOrders()); // 2 distinct orders in ledger
        assertEquals("Widget", response.getTopProducts().get(0).getProductName());
    }
}
