package com.bloodbank.controller;

import com.bloodbank.entity.ActivityLog;
import com.bloodbank.repository.ActivityLogRepository;
import com.bloodbank.service.BackupRestoreService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
@Slf4j
public class AdminController {

    private final ActivityLogRepository logRepository;
    private final BackupRestoreService backupRestoreService;

    @GetMapping("/logs")
    public String showLogs(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "15") int size,
            Model model) {
        
        Page<ActivityLog> logPage = logRepository.findAllByOrderByTimestampDesc(PageRequest.of(page, size));
        model.addAttribute("logs", logPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", logPage.getTotalPages());
        model.addAttribute("totalItems", logPage.getTotalElements());
        return "admin/logs";
    }

    @GetMapping("/settings")
    public String showSettings() {
        return "admin/settings";
    }

    @GetMapping("/backup/export")
    public void exportBackup(HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        String filename = "blood_bank_backup_" + LocalDateTime.now().toString().replace(":", "-") + ".json";
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\"");

        try {
            String json = backupRestoreService.exportDatabaseToJson();
            response.getWriter().write(json);
        } catch (Exception e) {
            log.error("Failed to export backup", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error generating backup: " + e.getMessage());
        }
    }

    @PostMapping("/backup/restore")
    public String restoreBackup(@RequestParam("file") MultipartFile file, Model model) {
        if (file.isEmpty()) {
            return "redirect:/admin/settings?error=empty_file";
        }

        try {
            backupRestoreService.restoreDatabaseFromJson(file.getInputStream());
            return "redirect:/admin/settings?success=restored";
        } catch (Exception e) {
            log.error("Failed to restore backup", e);
            return "redirect:/admin/settings?error=" + e.getMessage();
        }
    }
}
