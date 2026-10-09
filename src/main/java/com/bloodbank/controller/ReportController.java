package com.bloodbank.controller;

import com.bloodbank.service.ReportService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;

    @GetMapping
    public String index(Model model) {
        model.addAttribute("defaultStart", LocalDate.now().withDayOfMonth(1));
        model.addAttribute("defaultEnd", LocalDate.now());
        return "reports/index";
    }

    @GetMapping("/donations/pdf")
    public void downloadDonationsPdf(
            @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            HttpServletResponse response) throws IOException {
        
        if (start == null) start = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = LocalDate.now();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"donations_report_" + start + "_to_" + end + ".pdf\"");

        try {
            reportService.generateDonationReportPdf(response.getOutputStream(), start, end);
        } catch (Exception e) {
            log.error("Failed to generate donations PDF", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report");
        }
    }

    @GetMapping("/donations/excel")
    public void downloadDonationsExcel(
            @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            HttpServletResponse response) throws IOException {
        
        if (start == null) start = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = LocalDate.now();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"donations_report_" + start + "_to_" + end + ".xlsx\"");

        try {
            reportService.generateDonationReportExcel(response.getOutputStream(), start, end);
        } catch (Exception e) {
            log.error("Failed to generate donations Excel", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report");
        }
    }

    @GetMapping("/stock/pdf")
    public void downloadStockPdf(HttpServletResponse response) throws IOException {
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"stock_report_" + LocalDate.now() + ".pdf\"");

        try {
            reportService.generateStockReportPdf(response.getOutputStream());
        } catch (Exception e) {
            log.error("Failed to generate stock PDF", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report");
        }
    }

    @GetMapping("/stock/excel")
    public void downloadStockExcel(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"stock_report_" + LocalDate.now() + ".xlsx\"");

        try {
            reportService.generateStockReportExcel(response.getOutputStream());
        } catch (Exception e) {
            log.error("Failed to generate stock Excel", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report");
        }
    }

    @GetMapping("/requests/pdf")
    public void downloadRequestsPdf(
            @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            HttpServletResponse response) throws IOException {
        
        if (start == null) start = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = LocalDate.now();

        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=\"requests_report_" + start + "_to_" + end + ".pdf\"");

        try {
            reportService.generateRequestReportPdf(response.getOutputStream(), start, end);
        } catch (Exception e) {
            log.error("Failed to generate requests PDF", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report");
        }
    }

    @GetMapping("/requests/excel")
    public void downloadRequestsExcel(
            @RequestParam(value = "start", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(value = "end", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
            HttpServletResponse response) throws IOException {
        
        if (start == null) start = LocalDate.now().withDayOfMonth(1);
        if (end == null) end = LocalDate.now();

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"requests_report_" + start + "_to_" + end + ".xlsx\"");

        try {
            reportService.generateRequestReportExcel(response.getOutputStream(), start, end);
        } catch (Exception e) {
            log.error("Failed to generate requests Excel", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating report");
        }
    }
}
