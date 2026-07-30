package com.shopstack.modules.cart.controller;

import com.shopstack.modules.cart.dto.requests.AddCartItemRequest;
import com.shopstack.modules.cart.dto.requests.UpdateCartItemRequest;
import com.shopstack.modules.cart.entity.Cart;
import com.shopstack.modules.cart.service.CartService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {
    private final CartService cartService;

    @GetMapping
    public ResponseEntity<Cart> getCart(@RequestParam UUID userId) {
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<Cart> addItem(@RequestParam UUID userId, @RequestBody AddCartItemRequest request) {
        return ResponseEntity.ok(cartService.addItem(userId, request.getProductId(), request.getQuantity()));
    }

/**
     * POST /api/v1/cart/add-with-validation
     * Add to cart with detailed validation messages
     * Body: { "productId": "uuid", "quantity": 1 }
     */
    @PostMapping("/add-with-validation")
    public ResponseEntity<?> addToCartWithValidation(
            @RequestParam UUID userId, 
            @RequestBody AddCartItemRequest request) {
        var result = cartService.addToCartWithValidation(userId, request.getProductId(), request.getQuantity());
        
        if ((boolean) result.get("success")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    


    @PatchMapping("/items/{itemId}")
    public ResponseEntity<Cart> updateItem(@RequestParam UUID userId, @PathVariable UUID itemId, @RequestBody UpdateCartItemRequest request) {
        return ResponseEntity.ok(cartService.updateItemQuantity(userId, itemId, request.getQuantity()));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(@RequestParam UUID userId, @PathVariable UUID itemId) {
        cartService.removeItem(userId, itemId);
        return ResponseEntity.noContent().build();
    }
}