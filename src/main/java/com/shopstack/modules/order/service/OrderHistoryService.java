package com.shopstack.modules.order.service;

import com.shopstack.modules.order.entity.OrderHistory;
import com.shopstack.modules.order.repository.OrderHistoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderHistoryService {
    
    private final OrderHistoryRepository historyRepository;
    private final OrderService orderService;
    
    // =========================================
    // GET ORDER TIMELINE
    // =========================================
    public List<OrderHistory> getOrderTimeline(String orderId) {
        log.info("📜 Getting timeline for order: {}", orderId);
        return historyRepository.findByOrderIdOrderByChangedAtDesc(orderId);
    }
    
    // =========================================
    // GET ORDER TIMELINE BY ORDER NUMBER
    // =========================================
    public List<OrderHistory> getOrderTimelineByOrderNumber(String orderNumber) {
        log.info("📜 Getting timeline for order: {}", orderNumber);
        return historyRepository.findByOrderNumberOrderByChangedAtDesc(orderNumber);
    }
    
    // =========================================
    // GET ORDER STATUS HISTORY
    // =========================================
    public List<OrderHistory> getOrderStatusHistory(String orderId) {
        log.info("📊 Getting status history for order: {}", orderId);
        return historyRepository.findByOrderIdOrderByChangedAtDesc(orderId);
    }
}