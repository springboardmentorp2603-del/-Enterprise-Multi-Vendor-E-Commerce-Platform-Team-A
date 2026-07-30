package com.shopstack.modules.order.controller;

import com.shopstack.modules.order.entity.Order;
import com.shopstack.common.security.SecurityUtil;
import com.shopstack.modules.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {
    private final OrderService orderService;
    private final SecurityUtil securityUtil;
//
   @GetMapping
    public ResponseEntity<List<Order>> getOrders() {
    // FIX: If there is no userId, just return an empty list instead of crashing
   // if (userId == null) {
     //   return ResponseEntity.ok(java.util.Collections.emptyList());
   // }
    return ResponseEntity.ok(orderService.getOrders(securityUtil.getCurrentUser().getId()));
    }

    @GetMapping("/vendor")
    public ResponseEntity<List<Order>> getVendorOrders() {
        return ResponseEntity.ok(orderService.getAllOrders());
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.getOrderById(orderId));
    }

    @RequestMapping(value = "/{orderId}/status", method = {RequestMethod.PATCH, RequestMethod.PUT})
    public ResponseEntity<Order> updateOrderStatus(@PathVariable UUID orderId, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, status));
    }

    @PostMapping("/{orderId}/cancel")
    public ResponseEntity<Order> cancelOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, "CANCELLED"));
    }

    @PostMapping("/{orderId}/return")
    public ResponseEntity<Order> returnOrder(@PathVariable UUID orderId) {
        return ResponseEntity.ok(orderService.updateStatus(orderId, "RETURNED"));
    }

    @GetMapping("/{orderId}/timeline")
    public ResponseEntity<List<java.util.Map<String, Object>>> getOrderTimeline(@PathVariable UUID orderId) {
        Order order = orderService.getOrderById(orderId);
        List<java.util.Map<String, Object>> timeline = new java.util.ArrayList<>();
        if (order != null) {
            timeline.add(java.util.Map.of("status", "PLACED", "timestamp", order.getCreatedAt() != null ? order.getCreatedAt().toString() : "Recent"));
            timeline.add(java.util.Map.of("status", order.getStatus() != null ? order.getStatus() : "PROCESSING", "timestamp", "Current"));
        }
        return ResponseEntity.ok(timeline);
    }

    @GetMapping("/{orderId}/tracking")
    public ResponseEntity<java.util.Map<String, Object>> getOrderTracking(@PathVariable UUID orderId) {
        Order order = orderService.getOrderById(orderId);
        java.util.Map<String, Object> tracking = new java.util.HashMap<>();
        tracking.put("orderId", orderId.toString());
        tracking.put("status", order != null ? order.getStatus() : "IN_TRANSIT");
        tracking.put("carrier", "BlueDart Express");
        tracking.put("trackingNumber", "BD" + Math.abs(orderId.hashCode()));
        return ResponseEntity.ok(tracking);
    }
}