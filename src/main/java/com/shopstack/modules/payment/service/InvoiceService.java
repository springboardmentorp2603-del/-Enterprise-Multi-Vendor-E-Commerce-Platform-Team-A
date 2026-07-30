package com.shopstack.modules.payment.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.shopstack.modules.payment.entity.Invoice;
import com.shopstack.modules.payment.repository.InvoiceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class InvoiceService {

    @Autowired
    private InvoiceRepository invoiceRepository;

   public Invoice generateInvoice(UUID orderId, UUID paymentId, Double amount, Double gstAmount, String paymentMethod) { 

        Invoice invoice = new Invoice();
        invoice.setOrderId(orderId);
        invoice.setPaymentId(paymentId);
        invoice.setAmount(amount);
        invoice.setGstAmount(gstAmount);
        invoice.setInvoiceNumber(generateInvoiceNumber());

        String pdfPath = generatePdfInvoice(invoice, paymentMethod);
        invoice.setPdfPath(pdfPath);

        return invoiceRepository.save(invoice);
    }

    private String generatePdfInvoice(Invoice invoice, String paymentMethod) {
        String filename = "invoice_" + invoice.getInvoiceNumber() + ".pdf";
        java.io.File dir = new java.io.File("invoice-files");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        String filePath = "invoice-files/" + filename;
        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
           double subtotal = invoice.getAmount() - invoice.getGstAmount();
            document.add(new Paragraph("ShopStack Invoice"));
            document.add(new Paragraph("Invoice Number: " + invoice.getInvoiceNumber()));
            document.add(new Paragraph("Order ID: " + invoice.getOrderId()));
            document.add(new Paragraph("Payment Method: " + (paymentMethod != null ? paymentMethod : "N/A")));
            document.add(new Paragraph("Subtotal: Rs. " + String.format("%.2f", subtotal)));
            document.add(new Paragraph("GST (18%): Rs. " + String.format("%.2f", invoice.getGstAmount())));
            document.add(new Paragraph("Total Paid: Rs. " + String.format("%.2f", invoice.getAmount())));
            document.close();
            return filePath;
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error generating PDF invoice", e);
        }
    }

    private String generateInvoiceNumber() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}