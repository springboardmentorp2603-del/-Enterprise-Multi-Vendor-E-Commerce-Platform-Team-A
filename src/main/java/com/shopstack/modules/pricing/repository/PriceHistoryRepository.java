package com.shopstack.modules.pricing.repository;

import com.shopstack.modules.pricing.entity.PriceHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PriceHistoryRepository extends JpaRepository<PriceHistory, UUID> {
    List<PriceHistory> findByProductIdOrderByCreatedAtDesc(UUID productId);
    List<PriceHistory> findByVendorIdOrderByCreatedAtDesc(Long vendorId);
}