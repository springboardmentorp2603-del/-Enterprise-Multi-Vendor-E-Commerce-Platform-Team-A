package com.shopstack.modules.report.service;

import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;
import com.shopstack.modules.report.dto.responses.SalesReportRow;
import com.shopstack.modules.report.dto.responses.VendorReportRow;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

@Service
public class ReportExportService {

    private static final String REPORT_DIR = "report-files";

    public String generateSalesReportPdf(List<SalesReportRow> rows, LocalDate from, LocalDate to) {
        ensureDirExists();
        String filePath = REPORT_DIR + "/sales_report_" + System.currentTimeMillis() + ".pdf";

        Document document = new Document();
        try {
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();
            document.add(new Paragraph("ShopStack Sales Report"));
            document.add(new Paragraph("Period: " + from + " to " + to));
            document.add(new Paragraph(" "));

            for (SalesReportRow row : rows) {
                document.add(new Paragraph(
                        row.getDate() + "  |  Orders: " + row.getOrderCount()
                                + "  |  Revenue: Rs. " + row.getRevenue()));
            }
            document.close();
            return filePath;
        } catch (DocumentException | IOException e) {
            throw new RuntimeException("Error generating sales report PDF", e);
        }
    }

    public String generateSalesReportExcel(List<SalesReportRow> rows) {
        ensureDirExists();
        String filePath = REPORT_DIR + "/sales_report_" + System.currentTimeMillis() + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet("Sales Report");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Date");
            header.createCell(1).setCellValue("Orders");
            header.createCell(2).setCellValue("Revenue");

            int rowIdx = 1;
            for (SalesReportRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getDate().toString());
                row.createCell(1).setCellValue(r.getOrderCount());
                row.createCell(2).setCellValue(r.getRevenue().doubleValue());
            }
            workbook.write(out);
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Error generating sales report Excel", e);
        }
    }

    public String generateVendorReportExcel(List<VendorReportRow> rows) {
        ensureDirExists();
        String filePath = REPORT_DIR + "/vendor_report_" + System.currentTimeMillis() + ".xlsx";

        try (Workbook workbook = new XSSFWorkbook();
             FileOutputStream out = new FileOutputStream(filePath)) {

            Sheet sheet = workbook.createSheet("Vendor Report");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Vendor");
            header.createCell(1).setCellValue("Orders");
            header.createCell(2).setCellValue("Revenue");
            header.createCell(3).setCellValue("Commission Earned");

            int rowIdx = 1;
            for (VendorReportRow r : rows) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(r.getBusinessName());
                row.createCell(1).setCellValue(r.getTotalOrders());
                row.createCell(2).setCellValue(r.getTotalRevenue().doubleValue());
                row.createCell(3).setCellValue(r.getCommissionEarned().doubleValue());
            }
            workbook.write(out);
            return filePath;
        } catch (IOException e) {
            throw new RuntimeException("Error generating vendor report Excel", e);
        }
    }

    private void ensureDirExists() {
        File dir = new File(REPORT_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}