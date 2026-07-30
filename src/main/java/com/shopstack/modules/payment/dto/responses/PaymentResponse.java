package com.shopstack.modules.payment.dto.responses;

import java.util.UUID;

public class PaymentResponse {

    private UUID orderId;
    private String razorpayOrderId;
    private Double amount;
    private String currency;
    private String status;

    public PaymentResponse(UUID orderId, String razorpayOrderId, Double amount, String currency, String status) {
        this.orderId = orderId;
        this.razorpayOrderId = razorpayOrderId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }

    public UUID getOrderId() { return orderId; }
    public String getRazorpayOrderId() { return razorpayOrderId; }
    public Double getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getStatus() { return status; }
}