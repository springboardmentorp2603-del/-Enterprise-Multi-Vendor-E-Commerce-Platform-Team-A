package com.shopstack.modules.order.controller;

import com.shopstack.modules.order.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
public class CheckoutController {
    private final CheckoutService checkoutService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> processCheckout(@RequestBody Map<String, Object> checkoutRequest) {
        return ResponseEntity.ok(checkoutService.processCheckout(checkoutRequest));
    }
}
