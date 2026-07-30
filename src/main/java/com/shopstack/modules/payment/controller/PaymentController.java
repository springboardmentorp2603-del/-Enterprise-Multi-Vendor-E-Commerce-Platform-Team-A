package com.shopstack.modules.payment.controller;

import com.shopstack.modules.payment.dto.requests.PaymentCreateRequest;
import com.shopstack.modules.payment.dto.requests.PaymentVerifyRequest;
import com.shopstack.modules.payment.dto.responses.PaymentResponse;
import com.shopstack.modules.payment.entity.Payment;
import com.shopstack.modules.payment.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PostMapping("/create")
    public PaymentResponse createPayment(@RequestBody PaymentCreateRequest request) throws Exception {

        Payment payment = paymentService.createPayment(
                request.getOrderId(),
                request.getAmount(),
                request.getCurrency()
        );

        return new PaymentResponse(
                payment.getOrderId(),
                payment.getRazorpayOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus()
        );
    }

    @PostMapping("/verify")
    public PaymentResponse verifyPayment(@RequestBody PaymentVerifyRequest request) {

        Payment payment = paymentService.verifyPayment(
                request.getRazorpayOrderId(),
                request.getRazorpayPaymentId(),
                request.getRazorpaySignature()
        );

        return new PaymentResponse(
                payment.getOrderId(),
                payment.getRazorpayOrderId(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getStatus()
        );
    }

    @PostMapping("/webhook")
    public String webhook(@RequestBody String payload) {
        return "Webhook received";
    }
}