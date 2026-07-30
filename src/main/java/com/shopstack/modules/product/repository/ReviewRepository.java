package com.shopstack.modules.product.repository;

import com.shopstack.modules.product.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByProduct_Id(UUID productId);
}