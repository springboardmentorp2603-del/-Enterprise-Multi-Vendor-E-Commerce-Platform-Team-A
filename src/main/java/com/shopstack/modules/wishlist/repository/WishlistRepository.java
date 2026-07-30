package com.shopstack.modules.wishlist.repository;

import com.shopstack.modules.wishlist.entity.Wishlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {
    
    /**
     * Find wishlist by user ID
     * Each user has exactly one wishlist
     */
    Optional<Wishlist> findByUserId(UUID userId);
    
    /**
     * Check if user has a wishlist
     */
    boolean existsByUserId(UUID userId);
}