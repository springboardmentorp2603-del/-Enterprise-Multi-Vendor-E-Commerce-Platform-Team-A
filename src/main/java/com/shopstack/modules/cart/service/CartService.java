package com.shopstack.modules.cart.service;

import com.shopstack.modules.cart.entity.Cart;
import com.shopstack.modules.cart.entity.CartItem;
import com.shopstack.modules.cart.repository.CartItemRepository;
import com.shopstack.modules.cart.repository.CartRepository;
import com.shopstack.modules.product.entity.Product;
import com.shopstack.modules.product.repository.ProductRepository;
import com.shopstack.modules.user.entity.User;
import com.shopstack.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    // ✅ CART LIMIT CONSTANT
    private static final int MAX_CART_ITEMS_PER_PRODUCT = 10;

    public Cart getCart(UUID userId) {
        return getOrCreateCart(userId);
    }

    /**
     * ✅ NEW METHOD: Add item with validation
     * Returns detailed response with messages
     */
    public Map<String, Object> addToCartWithValidation(UUID userId, UUID productId, Integer quantity) {
        Map<String, Object> response = new HashMap<>();

        try {
            Cart cart = getOrCreateCart(userId);

            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new RuntimeException("Product not found: " + productId));

            // ✅ Check 1: Product out of stock
            if (product.getStockQuantity() == null || product.getStockQuantity() <= 0) {
                response.put("success", false);
                response.put("status", "OUT_OF_STOCK");
                response.put("message", "❌ " + product.getProductName() + " is out of stock");
                return response;
            }

            // Get existing item if any
            CartItem existing = cartItemRepository.findByCart_IdAndProduct_Id(cart.getId(), productId)
                    .orElse(null);

            int currentQuantity = existing != null ? existing.getQuantity() : 0;
            int newTotalQuantity = currentQuantity + quantity;

            // ✅ Check 2: Cart limit exceeded (max 10 per product)
            if (newTotalQuantity > MAX_CART_ITEMS_PER_PRODUCT) {
                response.put("success", false);
                response.put("status", "CART_LIMIT_EXCEEDED");
                response.put("message", "🛑 Maximum " + MAX_CART_ITEMS_PER_PRODUCT + " items per product allowed. " +
                        "You have " + currentQuantity + " in cart.");
                response.put("maxAllowed", MAX_CART_ITEMS_PER_PRODUCT);
                response.put("currentInCart", currentQuantity);
                response.put("availableToAdd", MAX_CART_ITEMS_PER_PRODUCT - currentQuantity);
                return response;
            }

            // ✅ Check 3: Not enough stock
            if (product.getStockQuantity() < newTotalQuantity) {
                response.put("success", false);
                response.put("status", "INSUFFICIENT_STOCK");
                response.put("message", "⚠️ Only " + product.getStockQuantity() + " items available. " +
                        "You're trying to add " + newTotalQuantity + ".");
                response.put("available", product.getStockQuantity());
                response.put("requested", newTotalQuantity);
                return response;
            }

            // ✅ All validations passed - add to cart
            if (existing != null) {
                existing.setQuantity(newTotalQuantity);
                cartItemRepository.save(existing);
            } else {
                CartItem item = new CartItem();
                item.setCart(cart);
                item.setProduct(product);
                item.setQuantity(quantity);
                cart.getItems().add(item);
                cartItemRepository.save(item);
            }

            cartRepository.save(cart);

            // ✅ Return success response
            response.put("success", true);
            response.put("status", "ADDED");
            response.put("message", "✅ " + quantity + " " + product.getProductName() + "(s) added to cart");
            response.put("cartTotal", cart.getItems().size());
            response.put("itemQuantity", quantity);
            return response;

        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("status", "ERROR");
            response.put("message", "❌ " + e.getMessage());
            return response;
        }
    }

    /**
     * EXISTING METHOD (with added validation for cart limit)
     */
public Cart addItem(UUID userId, UUID productId, Integer quantity) {
    Cart cart = getOrCreateCart(userId);

    Product product = productRepository.findById(productId)
            .orElseThrow(() -> new RuntimeException(
                    "Product not found: " + productId
            ));

    if (quantity == null || quantity <= 0) {
        throw new RuntimeException("Quantity must be greater than 0");
    }

    CartItem existing = cartItemRepository
            .findByCart_IdAndProduct_Id(cart.getId(), productId)
            .orElse(null);

    int currentQuantity = existing != null
            ? existing.getQuantity()
            : 0;

    int newTotal = currentQuantity + quantity;

    // 1. Maximum 10 per product
    if (newTotal > MAX_CART_ITEMS_PER_PRODUCT) {
        throw new RuntimeException(
                "🛑 Maximum " + MAX_CART_ITEMS_PER_PRODUCT +
                " items per product allowed"
        );
    }

    // 2. Check actual stock against TOTAL quantity
    if (product.getStockQuantity() == null ||
            product.getStockQuantity() < newTotal) {

        throw new RuntimeException(
                "⚠️ Only " + product.getStockQuantity() +
                " items available for: " +
                product.getProductName()
        );
    }

    // 3. Update existing item
    if (existing != null) {
        existing.setQuantity(newTotal);
        cartItemRepository.save(existing);
    } else {

        CartItem item = new CartItem();
        item.setCart(cart);
        item.setProduct(product);
        item.setQuantity(quantity);

        cart.getItems().add(item);
        cartItemRepository.save(item);
    }

    return cartRepository.findById(cart.getId())
            .orElseThrow();
}

    /**
     * EXISTING METHOD (with added validation for cart limit)
     */
    public Cart updateItemQuantity(UUID userId, UUID cartItemId, Integer quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("This item does not belong to your cart");
        }

        if (quantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            // ✅ NEW: Check cart limit
            if (quantity > MAX_CART_ITEMS_PER_PRODUCT) {
                throw new RuntimeException("🛑 Maximum " + MAX_CART_ITEMS_PER_PRODUCT + " items per product allowed");
            }

            if (item.getProduct().getStockQuantity() < quantity) {
                throw new RuntimeException("⚠️ Only " + item.getProduct().getStockQuantity() + 
                        " items available for: " + item.getProduct().getProductName());
            }
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }

        return cartRepository.findById(cart.getId()).orElseThrow();
    }

    /**
     * EXISTING METHOD
     */
    public void removeItem(UUID userId, UUID cartItemId) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Cart item not found: " + cartItemId));

        if (!item.getCart().getId().equals(cart.getId())) {
            throw new RuntimeException("This item does not belong to your cart");
        }

        cartItemRepository.delete(item);
    }

    /**
     * Clear entire cart
     */
    public void clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    /**
     * Get cart item count
     */
    public int getCartItemCount(UUID userId) {
        Cart cart = cartRepository.findByUserId(userId).orElse(null);
        if (cart == null) {
            return 0;
        }
        return cart.getItems().size();
    }

    private Cart getOrCreateCart(UUID userId) {
        return cartRepository.findByUserId(userId).orElseGet(() -> {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("User not found: " + userId));
            Cart newCart = new Cart();
            newCart.setUser(user);
            return cartRepository.save(newCart);
        });
    }
}