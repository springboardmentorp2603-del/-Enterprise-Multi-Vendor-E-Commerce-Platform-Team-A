package com.shopstack.modules.wishlist.service;

import com.shopstack.modules.wishlist.entity.Wishlist;
import com.shopstack.modules.wishlist.entity.WishlistItem;
import com.shopstack.modules.wishlist.repository.WishlistRepository;
import com.shopstack.modules.wishlist.repository.WishlistItemRepository;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.repository.UserRepository;
import com.shopstack.modules.cart.entity.Cart;
import com.shopstack.modules.cart.entity.CartItem;
import com.shopstack.modules.cart.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final CartRepository cartRepository;

    /**
     * Get or create wishlist for user
     */
    public Wishlist getOrCreateWishlist(UUID userId) {
        return wishlistRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found: " + userId));
                    
                    Wishlist wishlist = new Wishlist();
                    wishlist.setUser(user);
                    return wishlistRepository.save(wishlist);
                });
    }

    /**
     * Get user's wishlist with all items
     */
    public Wishlist getWishlist(UUID userId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        return wishlist;
    }

    /**
     * Add product to wishlist
     */
    public WishlistItem addToWishlist(UUID userId, UUID productId) {
        // Get wishlist (create if not exists)
        Wishlist wishlist = getOrCreateWishlist(userId);

        // Check if product exists
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

        // Check if product already in wishlist
        if (wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId)) {
            throw new RuntimeException("Product already in wishlist");
        }

        // Create wishlist item
        WishlistItem item = new WishlistItem();
        item.setWishlist(wishlist);
        item.setProduct(product);
        item.setProductName(product.getProductName());
        item.setProductPrice(product.getPrice().doubleValue());
        item.setProductImage(product.getImageUrl());

        item = wishlistItemRepository.save(item);
        wishlist.addItem(item);
        wishlistRepository.save(wishlist);

        return item;
    }

    /**
     * Remove product from wishlist
     */
    public void removeFromWishlist(UUID userId, UUID wishlistItemId) {
        Wishlist wishlist = getOrCreateWishlist(userId);

        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found: " + wishlistItemId));

        // Verify item belongs to user's wishlist
        if (!item.getWishlist().getId().equals(wishlist.getId())) {
            throw new RuntimeException("Unauthorized: Item does not belong to user's wishlist");
        }

        wishlist.removeItem(item);
        wishlistItemRepository.deleteById(wishlistItemId);
        wishlistRepository.save(wishlist);
    }

    /**
     * Check if product is in user's wishlist
     */
    public boolean isInWishlist(UUID userId, UUID productId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId).orElse(null);
        if (wishlist == null) {
            return false;
        }
        return wishlistItemRepository.existsByWishlistIdAndProductId(wishlist.getId(), productId);
    }

    /**
     * Add product to cart and remove from wishlist
     * ✅ KEY METHOD: User clicks "Add to Cart" from wishlist
     */
    public void addToCartAndRemoveFromWishlist(UUID userId, UUID wishlistItemId) {
        // Get wishlist item
        WishlistItem item = wishlistItemRepository.findById(wishlistItemId)
                .orElseThrow(() -> new RuntimeException("Wishlist item not found: " + wishlistItemId));

        // Verify it belongs to this user
        Wishlist wishlist = item.getWishlist();
        if (!wishlist.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized: Item does not belong to user");
        }

        Product product = item.getProduct();

        // Check stock before adding to cart
        if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
            throw new RuntimeException("Product is out of stock");
        }

        // Add to cart (or get existing cart)
        Cart cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> {
                    User user = userRepository.findById(userId)
                            .orElseThrow(() -> new RuntimeException("User not found"));
                    Cart newCart = new Cart();
                    newCart.setUser(user);
                    return cartRepository.save(newCart);
                });

        // Check if product already in cart
        CartItem existingCartItem = cart.getItems().stream()
                .filter(ci -> ci.getProduct().getId().equals(product.getId()))
                .findFirst()
                .orElse(null);

        if (existingCartItem != null) {
            // Update quantity
            existingCartItem.setQuantity(existingCartItem.getQuantity() + 1);
        } else {
            // Create new cart item
            CartItem cartItem = new CartItem();
            cartItem.setCart(cart);
            cartItem.setProduct(product);
            cartItem.setQuantity(1);
            cart.getItems().add(cartItem);
        }

        // Save cart
        cartRepository.save(cart);

        // Remove from wishlist
        removeFromWishlist(userId, wishlistItemId);
    }

    /**
     * Clear entire wishlist
     */
    public void clearWishlist(UUID userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId).orElse(null);
        if (wishlist != null) {
            wishlist.getItems().clear();
            wishlistRepository.save(wishlist);
        }
    }

    /**
     * Get wishlist items count
     */
    public long getWishlistItemsCount(UUID userId) {
        Wishlist wishlist = wishlistRepository.findByUserId(userId).orElse(null);
        if (wishlist == null) {
            return 0;
        }
        return wishlist.getItems().size();
    }

    /**
     * Get wishlist items as list of products
     */
    public List<Product> getWishlistProducts(UUID userId) {
        Wishlist wishlist = getOrCreateWishlist(userId);
        return wishlist.getItems().stream()
                .map(WishlistItem::getProduct)
                .collect(Collectors.toList());
    }
}