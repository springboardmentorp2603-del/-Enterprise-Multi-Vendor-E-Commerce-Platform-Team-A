package com.shopstack.modules.order.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
import com.shopstack.common.enums.OrderStatus;

@Entity
@Table(name = "order_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderHistory {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    private String orderId;
    private String orderNumber;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus status;
    
    @Enumerated(EnumType.STRING)
    private OrderStatus previousStatus;
    
    @CreationTimestamp
    private LocalDateTime changedAt;
    
    private String changedBy;
    private String reason;
    private String details;
}