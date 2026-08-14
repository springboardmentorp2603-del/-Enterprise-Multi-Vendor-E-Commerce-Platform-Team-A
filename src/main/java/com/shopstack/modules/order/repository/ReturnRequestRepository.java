package com.shopstack.modules.order.repository;

import com.shopstack.modules.order.entity.ReturnRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReturnRequestRepository extends JpaRepository<ReturnRequest, UUID> {
    List<ReturnRequest> findByOrderId(UUID orderId);
}
