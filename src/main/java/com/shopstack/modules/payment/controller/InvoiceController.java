package com.shopstack.modules.payment.controller;

import com.shopstack.modules.payment.entity.Invoice;
import com.shopstack.modules.payment.repository.InvoiceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceRepository invoiceRepository;

    @GetMapping("/order/{orderId}")
    public ResponseEntity<Invoice> getInvoiceByOrder(@PathVariable UUID orderId) {
        return invoiceRepository.findByOrderId(orderId)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new RuntimeException("No invoice found for order: " + orderId));
    }
}