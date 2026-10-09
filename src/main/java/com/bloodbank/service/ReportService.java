package com.bloodbank.service;

import com.bloodbank.entity.BloodDonation;
import com.bloodbank.entity.BloodInventory;
import com.bloodbank.entity.BloodRequest;
import com.bloodbank.repository.BloodDonationRepository;
import com.bloodbank.repository.BloodInventoryRepository;
import com.bloodbank.repository.BloodRequestRepository;
import com.lowagie.text.Font;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportService {

    private final BloodDonationRepository donationRepository;
    private final BloodInventoryRepository inventoryRepository;
    private final BloodRequestRepository requestRepository;

    // ==========================================
    // DONATION REPORTS (PDF & Excel)
    // ==========================================

    public void generateDonationReportPdf(OutputStream out, LocalDate start, LocalDate end) throws DocumentException {
        List<BloodDonation> donations = donationRepository.findDonationsInRange(start, end);
        
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        // Title
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.RED);
        Paragraph title = new Paragraph("Blood Donation Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph dates = new Paragraph("Period: " + start + " to " + end, FontFactory.getFont(FontFactory.HELVETICA, 10));
        dates.setAlignment(Element.ALIGN_CENTER);
        dates.setSpacingAfter(20);
        document.add(dates);

        // Table
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 2.5f, 2.0f, 1.5f, 1.5f, 2.0f});

        // Headers
        String[] headers = {"ID", "Donor Name", "Blood Group", "Units", "Date", "Status"};
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
            cell.setBackgroundColor(Color.RED);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        // Data
        for (BloodDonation donation : donations) {
            table.addCell(String.valueOf(donation.getId()));
            table.addCell(donation.getDonor().getFullName());
            table.addCell(donation.getBloodGroup());
            table.addCell(String.valueOf(donation.getUnits()));
            table.addCell(donation.getDonationDate().toString());
            table.addCell(donation.getStatus());
        }

        document.add(table);
        document.close();
    }

    public void generateDonationReportExcel(OutputStream out, LocalDate start, LocalDate end) throws IOException {
        List<BloodDonation> donations = donationRepository.findDonationsInRange(start, end);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Donations Report");

            // Header Style
            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            // Headers
            Row headerRow = sheet.createRow(0);
            String[] headers = {"ID", "Donor Name", "Blood Group", "Units", "Date", "Status", "Remarks"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // Data Rows
            int rowIdx = 1;
            for (BloodDonation donation : donations) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(donation.getId());
                row.createCell(1).setCellValue(donation.getDonor().getFullName());
                row.createCell(2).setCellValue(donation.getBloodGroup());
                row.createCell(3).setCellValue(donation.getUnits());
                row.createCell(4).setCellValue(donation.getDonationDate().toString());
                row.createCell(5).setCellValue(donation.getStatus());
                row.createCell(6).setCellValue(donation.getRemarks() != null ? donation.getRemarks() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    // ==========================================
    // STOCK REPORTS (PDF & Excel)
    // ==========================================

    public void generateStockReportPdf(OutputStream out) throws DocumentException {
        List<BloodInventory> activeStock = inventoryRepository.findByStatus("AVAILABLE");

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.RED);
        Paragraph title = new Paragraph("Blood Stock Status Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.5f, 2.0f, 1.5f, 2.5f, 2.5f});

        String[] headers = {"Batch ID", "Blood Group", "Units", "Received Date", "Expiry Date"};
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
            cell.setBackgroundColor(Color.DARK_GRAY);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (BloodInventory stock : activeStock) {
            table.addCell(String.valueOf(stock.getId()));
            table.addCell(stock.getBloodGroup());
            table.addCell(String.valueOf(stock.getUnits()));
            table.addCell(stock.getReceivedDate().toString());
            table.addCell(stock.getExpiryDate().toString());
        }

        document.add(table);
        document.close();
    }

    public void generateStockReportExcel(OutputStream out) throws IOException {
        List<BloodInventory> activeStock = inventoryRepository.findByStatus("AVAILABLE");

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Blood Stock");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Batch ID", "Blood Group", "Units", "Received Date", "Expiry Date", "Donor Name"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (BloodInventory stock : activeStock) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(stock.getId());
                row.createCell(1).setCellValue(stock.getBloodGroup());
                row.createCell(2).setCellValue(stock.getUnits());
                row.createCell(3).setCellValue(stock.getReceivedDate().toString());
                row.createCell(4).setCellValue(stock.getExpiryDate().toString());
                row.createCell(5).setCellValue(stock.getDonor() != null ? stock.getDonor().getFullName() : "Anonymous");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }

    // ==========================================
    // REQUEST REPORTS (PDF & Excel)
    // ==========================================

    public void generateRequestReportPdf(OutputStream out, LocalDate start, LocalDate end) throws DocumentException {
        List<BloodRequest> requests = requestRepository.findRequestsInRange(start, end);

        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, out);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Color.RED);
        Paragraph title = new Paragraph("Blood Requests Report", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        document.add(title);

        Paragraph dates = new Paragraph("Period: " + start + " to " + end, FontFactory.getFont(FontFactory.HELVETICA, 10));
        dates.setAlignment(Element.ALIGN_CENTER);
        dates.setSpacingAfter(20);
        document.add(dates);

        PdfPTable table = new PdfPTable(7);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{1.0f, 2.5f, 2.5f, 1.5f, 1.5f, 1.5f, 2.0f});

        String[] headers = {"ID", "Patient Name", "Hospital Name", "Blood Group", "Units", "Urgency", "Status"};
        Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headFont));
            cell.setBackgroundColor(Color.RED);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }

        for (BloodRequest request : requests) {
            table.addCell(String.valueOf(request.getId()));
            table.addCell(request.getPatientName());
            table.addCell(request.getHospital() != null ? request.getHospital().getName() : "Direct");
            table.addCell(request.getBloodGroup());
            table.addCell(String.valueOf(request.getUnitsRequired()));
            table.addCell(request.getUrgency());
            table.addCell(request.getStatus());
        }

        document.add(table);
        document.close();
    }

    public void generateRequestReportExcel(OutputStream out, LocalDate start, LocalDate end) throws IOException {
        List<BloodRequest> requests = requestRepository.findRequestsInRange(start, end);

        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Blood Requests");

            CellStyle headerStyle = workbook.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = workbook.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            Row headerRow = sheet.createRow(0);
            String[] headers = {"Request ID", "Patient Name", "Hospital Name", "Blood Group", "Units Required", "Urgency", "Status", "Needed By Date", "Notes"};
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            int rowIdx = 1;
            for (BloodRequest request : requests) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(request.getId());
                row.createCell(1).setCellValue(request.getPatientName());
                row.createCell(2).setCellValue(request.getHospital() != null ? request.getHospital().getName() : "Direct");
                row.createCell(3).setCellValue(request.getBloodGroup());
                row.createCell(4).setCellValue(request.getUnitsRequired());
                row.createCell(5).setCellValue(request.getUrgency());
                row.createCell(6).setCellValue(request.getStatus());
                row.createCell(7).setCellValue(request.getRequiredByDate().toString());
                row.createCell(8).setCellValue(request.getNotes() != null ? request.getNotes() : "");
            }

            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
        }
    }
}
