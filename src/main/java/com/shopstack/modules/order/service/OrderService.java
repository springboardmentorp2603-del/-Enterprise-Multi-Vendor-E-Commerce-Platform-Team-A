package com.shopstack.modules.order.service;

import com.shopstack.common.enums.OrderStatus;
import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getOrders(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId is required to fetch orders");
        }
        return orderRepository.findByUserId(userId);
    }

    public Order getOrderById(UUID orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found: " + orderId));
    }

    public Order updateStatus(UUID orderId, String status) {
        Order order = getOrderById(orderId);
        order.setStatus(status.toUpperCase());
        return orderRepository.save(order);
    }

    public void updateOrderStatus(UUID orderId, OrderStatus orderStatus, String s, String system) {
        Order order = orderRepository.getReferenceById(orderId);
        order.setStatus(String.valueOf(orderStatus));
        orderRepository.save(order);
    }
}