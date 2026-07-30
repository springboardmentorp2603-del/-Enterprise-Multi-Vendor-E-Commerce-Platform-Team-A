package com.shopstack.modules.order.repository;

import com.shopstack.modules.order.entity.OrderHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderHistoryRepository extends JpaRepository<OrderHistory, String> {
    
    // Find all history for an order, newest first
    List<OrderHistory> findByOrderIdOrderByChangedAtDesc(String orderId);
    
    // Find history for an order by order number
    List<OrderHistory> findByOrderNumberOrderByChangedAtDesc(String orderNumber);
}