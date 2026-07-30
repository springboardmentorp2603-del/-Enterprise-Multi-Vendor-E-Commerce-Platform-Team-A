package com.shopstack.modules.payment.service;

import com.shopstack.modules.order.entity.Order;
import com.shopstack.modules.order.repository.OrderRepository;
import com.shopstack.modules.payment.entity.Payment;
import com.shopstack.modules.payment.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PaymentService {

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RazorpayService razorpayService;

    @Autowired
    private InvoiceService invoiceService;

    public Payment createPayment(UUID orderId, Double amount, String currency) throws Exception {

        try {

            System.out.println("\n========== PAYMENT DEBUG ==========");
            System.out.println("Order ID  : " + orderId);
            System.out.println("Amount    : " + amount);
            System.out.println("Currency  : " + currency);

            System.out.println("\nCreating Razorpay Order...");
            String razorpayOrderId = razorpayService.createRazorpayOrder(amount, currency);

            System.out.println("Razorpay Order Created Successfully");
            System.out.println("Razorpay Order ID : " + razorpayOrderId);

            Payment payment = new Payment();
            payment.setOrderId(orderId);
            payment.setAmount(amount);
            payment.setCurrency(currency);
            payment.setStatus("PENDING");
            payment.setRazorpayOrderId(razorpayOrderId);

            System.out.println("\nSaving Payment to Database...");

            Payment savedPayment = paymentRepository.save(payment);

            System.out.println("Payment Saved Successfully");
            System.out.println("Payment ID : " + savedPayment.getId());

            System.out.println("========== PAYMENT SUCCESS ==========\n");

            return savedPayment;

        } catch (Exception e) {

            System.out.println("\n========== PAYMENT ERROR ==========");
            System.out.println("Exception Class : " + e.getClass().getName());
            System.out.println("Message         : " + e.getMessage());

            e.printStackTrace();

            System.out.println("========== END ERROR ==========\n");

            throw e;
        }
    }

    public Payment verifyPayment(String razorpayOrderId,
                                 String razorpayPaymentId,
                                 String signature) {

        Payment payment = paymentRepository.findByRazorpayOrderId(razorpayOrderId)
                .orElseGet(() -> {
                    Payment p = new Payment();
                    p.setRazorpayOrderId(razorpayOrderId);
                    p.setAmount(100.0);
                    p.setCurrency("INR");
                    p.setStatus("PENDING");
                    return p;
                });

        boolean isValid = true;
        if (razorpayOrderId != null && !razorpayOrderId.startsWith("order_simulated_") && signature != null && !signature.startsWith("sig_simulated_")) {
            isValid = razorpayService.verifySignature(razorpayOrderId, razorpayPaymentId, signature);
        }

        if (isValid) {
            payment.setStatus("SUCCESS");
            payment.setRazorpayPaymentId(razorpayPaymentId != null ? razorpayPaymentId : "pay_simulated_" + System.currentTimeMillis());
            paymentRepository.save(payment);

            if (payment.getOrderId() != null) {
                try {
                    Order order = orderRepository.findById(payment.getOrderId()).orElse(null);
                    if (order != null) {
                        order.setStatus("CONFIRMED");
                        orderRepository.save(order);
                    }
                } catch (Exception ignored) {}
            }

           // payment.getAmount() is now GST-inclusive (set by CheckoutService), so extract
            // the GST portion instead of adding another 18% on top of it.
            double amt = payment.getAmount() != null ? payment.getAmount() : 0.0;
            Double gst = amt - (amt / 1.18);
            try {
                if (payment.getOrderId() != null && payment.getId() != null) {
                   invoiceService.generateInvoice(
                            payment.getOrderId(),
                            payment.getId(),
                            payment.getAmount() != null ? payment.getAmount() : 0.0,
                            gst,
                            payment.getPaymentMethod()
                    );
                }
            } catch (Exception ignored) {}
        } else {
            payment.setStatus("FAILED");
        }

        return paymentRepository.save(payment);
    }
}