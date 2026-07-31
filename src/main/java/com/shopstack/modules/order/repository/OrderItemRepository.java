package com.shopstack.modules.order.repository;

import com.shopstack.modules.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, UUID> {

    @Query("SELECT oi FROM OrderItem oi WHERE oi.order.createdAt BETWEEN :from AND :to")
    List<OrderItem> findAllInRange(@Param("from") LocalDateTime from, @Param("to") LocalDateTime to);
}