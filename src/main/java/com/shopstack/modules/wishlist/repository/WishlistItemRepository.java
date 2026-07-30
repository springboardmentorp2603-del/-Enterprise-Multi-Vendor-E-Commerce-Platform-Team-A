package com.shopstack.modules.wishlist.repository;

import com.shopstack.modules.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.List;

@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {
    
    /**
     * Find item by wishlist ID and product ID
     * To check if product already in wishlist
     */
    Optional<WishlistItem> findByWishlistIdAndProductId(UUID wishlistId, UUID productId);
    
    /**
     * Find all items in a wishlist
     */
    List<WishlistItem> findByWishlistId(UUID wishlistId);
    
    /**
     * Delete item by ID
     */
    void deleteById(UUID id);
    
    /**
     * Check if item exists
     */
    boolean existsByWishlistIdAndProductId(UUID wishlistId, UUID productId);

    void deleteByProductId(UUID productId);
}