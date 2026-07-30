package com.shopstack.modules.payment.service;

import com.razorpay.RazorpayClient;
import com.razorpay.Order;
import com.razorpay.Utils;
import com.shopstack.modules.payment.config.RazorpayConfig;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RazorpayService {

    @Autowired
    private RazorpayConfig razorpayConfig;

    public String createRazorpayOrder(Double amount, String currency) throws Exception {

        RazorpayClient client = new RazorpayClient(
                razorpayConfig.getKeyId(),
                razorpayConfig.getKeySecret()
        );

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", (int) (amount * 100)); 
        orderRequest.put("currency", currency);
        orderRequest.put("receipt", "receipt_" + System.currentTimeMillis());

        Order order = client.orders.create(orderRequest);

        return order.get("id");
    }

    public boolean verifySignature(String razorpayOrderId, String razorpayPaymentId, String signature) {
        try {
            JSONObject attributes = new JSONObject();
            attributes.put("razorpay_order_id", razorpayOrderId);
            attributes.put("razorpay_payment_id", razorpayPaymentId);
            attributes.put("razorpay_signature", signature);

            return Utils.verifyPaymentSignature(attributes, razorpayConfig.getKeySecret());
        } catch (Exception e) {
            return false;
        }
    }
}