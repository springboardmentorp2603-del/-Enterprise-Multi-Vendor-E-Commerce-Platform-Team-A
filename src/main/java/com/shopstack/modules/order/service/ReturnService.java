package com.shopstack.modules.order.service;

import com.shopstack.common.enums.OrderStatus;
import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.entity.OrderItem;
import com.shopstack.modules.order.entity.ReturnRequest;
import com.shopstack.modules.order.repository.OrderRepository;
import com.shopstack.modules.order.repository.ReturnRequestRepository;
import com.shopstack.modules.order.repository.ShipmentRepository;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.inventory.service.InventoryService;
import com.shopstack.modules.notification.service.NotificationService;
import com.shopstack.modules.systemlog.service.SystemLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReturnService {

    private final ReturnRequestRepository returnRequestRepository;
    private final OrderRepository orderRepository;
    private final ShipmentRepository shipmentRepository;
    private final ProductRepository productRepository;
    private final InventoryService inventoryService;
    private final RefundService refundService;
    private final NotificationService notificationService;
    private final SystemLogService systemLogService;

    @Transactional
    public ReturnRequest initiateReturn(UUID orderId, Map<String, String> payload) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));

        // 1. Eligibility Check
        if (!"DELIVERED".equalsIgnoreCase(order.getStatus())) {
            throw new IllegalArgumentException("Only delivered orders can be returned.");
        }

        // 2. Validate Return Window (30 days)
        var shipmentOpt = shipmentRepository.findByOrderId(String.valueOf(orderId));
        if (shipmentOpt.isPresent()) {
            LocalDateTime deliveredAt = shipmentOpt.get().getDeliveredAt();
            if (deliveredAt != null && LocalDateTime.now().isAfter(deliveredAt.plusDays(30))) {
                throw new IllegalArgumentException("The 30-day return window for this order has expired.");
            }
        }

        ReturnRequest request = new ReturnRequest();
        request.setOrder(order);
        request.setReason(payload.getOrDefault("reason", "OTHER"));
        request.setRefundType(payload.getOrDefault("refundType", "ORIGINAL_PAYMENT"));
        request.setNotes(payload.getOrDefault("notes", ""));
        request.setStatus("RETURN_REQUESTED");

        order.setStatus("RETURN_REQUESTED");
        orderRepository.save(order);

        ReturnRequest saved = returnRequestRepository.save(request);

        // Send Notification to customer
        try {
            notificationService.sendNotification(
                com.shopstack.modules.notification.dto.SendNotificationRequest.builder()
                    .recipientId(order.getUser().getId())
                    .recipientType("CUSTOMER")
                    .type(com.shopstack.modules.notification.entity.NotificationType.ORDER_UPDATE)
                    .channel(com.shopstack.modules.notification.entity.NotificationChannel.IN_APP)
                    .title("Return Requested")
                    .message("Your return request for Order #" + orderId + " has been registered and is under review.")
                    .build()
            );
        } catch (Exception ignored) {}

        systemLogService.log("RETURN", "Return Requested", order.getUser() != null ? order.getUser().getId() : null, "Customer", "INFO", "Return requested: " + saved.getId() + " for Order: " + orderId);

        return saved;
    }

    public List<ReturnRequest> getAllReturnRequests() {
        return returnRequestRepository.findAll();
    }

    public List<ReturnRequest> getReturnRequestsByVendor(Long vendorId) {
        return returnRequestRepository.findAll().stream()
                .filter(r -> r.getOrder() != null && r.getOrder().getItems() != null &&
                        r.getOrder().getItems().stream().anyMatch(item -> {
                            try {
                                UUID pid = UUID.fromString(item.getProductId());
                                Product product = productRepository.findById(pid).orElse(null);
                                return product != null && vendorId.equals(product.getVendorId());
                            } catch (Exception e) {
                                return false;
                            }
                        }))
                .collect(java.util.stream.Collectors.toList());
    }

    @Transactional
    public ReturnRequest updateReturnStatus(UUID returnRequestId, String status) {
        ReturnRequest request = returnRequestRepository.findById(returnRequestId)
                .orElseThrow(() -> new RuntimeException("Return request not found: " + returnRequestId));
        
        String targetStatus = status.toUpperCase();
        request.setStatus(targetStatus);
        ReturnRequest saved = returnRequestRepository.save(request);

        Order order = request.getOrder();
        order.setStatus(targetStatus);
        orderRepository.save(order);

        UUID customerId = order.getUser() != null ? order.getUser().getId() : null;

        // Perform stock restock on RETURN_RECEIVED
        if ("RETURN_RECEIVED".equals(targetStatus)) {
            for (OrderItem item : order.getItems()) {
                try {
                    UUID productId = UUID.fromString(item.getProductId());
                    // Adjust inventory availableStock
                    inventoryService.adjustStock(productId, item.getQuantity(), customerId, "Returned product restocked");
                    
                    // Sync Product catalog stock quantity
                    Product prod = productRepository.findById(productId).orElse(null);
                    if (prod != null) {
                        prod.setStockQuantity((prod.getStockQuantity() != null ? prod.getStockQuantity() : 0) + item.getQuantity());
                        productRepository.save(prod);
                    }
                } catch (Exception ignored) {}
            }
            
            try {
                notificationService.sendNotification(
                    com.shopstack.modules.notification.dto.SendNotificationRequest.builder()
                        .recipientId(customerId)
                        .recipientType("CUSTOMER")
                        .type(com.shopstack.modules.notification.entity.NotificationType.ORDER_UPDATE)
                        .channel(com.shopstack.modules.notification.entity.NotificationChannel.IN_APP)
                        .title("Return Received")
                        .message("We received your returned items for Order #" + order.getId() + ". Refund approval is now underway.")
                        .build()
                );
            } catch (Exception ignored) {}

            systemLogService.log("RETURN", "Return Received", customerId, "Warehouse", "INFO", "Return received & restocked: " + returnRequestId);
        }

        if ("RETURN_APPROVED".equals(targetStatus)) {
            try {
                notificationService.sendNotification(
                    com.shopstack.modules.notification.dto.SendNotificationRequest.builder()
                        .recipientId(customerId)
                        .recipientType("CUSTOMER")
                        .type(com.shopstack.modules.notification.entity.NotificationType.ORDER_UPDATE)
                        .channel(com.shopstack.modules.notification.entity.NotificationChannel.IN_APP)
                        .title("Return Approved")
                        .message("Your return request for Order #" + order.getId() + " is approved. A pickup is being scheduled.")
                        .build()
                );
            } catch (Exception ignored) {}
        }

        if ("RETURN_PICKED".equals(targetStatus)) {
            try {
                notificationService.sendNotification(
                    com.shopstack.modules.notification.dto.SendNotificationRequest.builder()
                        .recipientId(customerId)
                        .recipientType("CUSTOMER")
                        .type(com.shopstack.modules.notification.entity.NotificationType.ORDER_UPDATE)
                        .channel(com.shopstack.modules.notification.entity.NotificationChannel.IN_APP)
                        .title("Return Picked Up")
                        .message("Courier has successfully picked up your return package for Order #" + order.getId() + ".")
                        .build()
                );
            } catch (Exception ignored) {}
        }

        if ("REFUND_INITIATED".equals(targetStatus)) {
            try {
                notificationService.sendNotification(
                    com.shopstack.modules.notification.dto.SendNotificationRequest.builder()
                        .recipientId(customerId)
                        .recipientType("CUSTOMER")
                        .type(com.shopstack.modules.notification.entity.NotificationType.PAYMENT)
                        .channel(com.shopstack.modules.notification.entity.NotificationChannel.IN_APP)
                        .title("Refund Initiated")
                        .message("Your refund of Rs. " + order.getTotalAmount() + " has been initiated.")
                        .build()
                );
            } catch (Exception ignored) {}
        }

        if ("REFUND_COMPLETED".equals(targetStatus) || "REFUNDED".equals(targetStatus)) {
            refundService.processRefund(saved);
            
            try {
                notificationService.sendNotification(
                    com.shopstack.modules.notification.dto.SendNotificationRequest.builder()
                        .recipientId(customerId)
                        .recipientType("CUSTOMER")
                        .type(com.shopstack.modules.notification.entity.NotificationType.PAYMENT)
                        .channel(com.shopstack.modules.notification.entity.NotificationChannel.IN_APP)
                        .title("Refund Completed")
                        .message("Your refund of Rs. " + order.getTotalAmount() + " has been credited successfully.")
                        .build()
                );
            } catch (Exception ignored) {}

            systemLogService.log("REFUND", "Refund Completed", customerId, "System", "INFO", "Refund completed: " + returnRequestId);
        }

        return saved;
    }
}
