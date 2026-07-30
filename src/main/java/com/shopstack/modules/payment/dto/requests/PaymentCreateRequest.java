package com.shopstack.modules.payment.dto.requests;

import java.util.UUID;

public class PaymentCreateRequest {

    private UUID orderId;
    private Double amount;
    private String currency;

    public UUID getOrderId() { return orderId; }
    public void setOrderId(UUID orderId) { this.orderId = orderId; }

    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
}