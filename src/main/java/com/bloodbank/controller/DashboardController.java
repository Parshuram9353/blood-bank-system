package com.bloodbank.controller;

import com.bloodbank.entity.BloodDonation;
import com.bloodbank.entity.BloodRequest;
import com.bloodbank.entity.Donor;
import com.bloodbank.entity.User;
import com.bloodbank.repository.ActivityLogRepository;
import com.bloodbank.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DashboardController {

    private final UserService userService;
    private final DonorService donorService;
    private final InventoryService inventoryService;
    private final DonationService donationService;
    private final RequestService requestService;
    private final ActivityLogRepository activityLogRepository;

    @GetMapping("/dashboard")
    public String dashboardRedirect() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }
        
        if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            return "redirect:/admin/dashboard";
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_STAFF"))) {
            return "redirect:/staff/dashboard";
        } else if (auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_DONOR"))) {
            return "redirect:/donor/dashboard";
        }
        
        return "redirect:/login";
    }

    @GetMapping("/admin/dashboard")
    public String adminDashboard(Model model) {
        model.addAttribute("totalDonors", donorService.countTotalDonors());
        model.addAttribute("availableUnits", inventoryService.getTotalAvailableUnits());
        model.addAttribute("totalRequests", requestService.countTotalRequests());
        model.addAttribute("pendingRequests", requestService.countPendingRequests());
        model.addAttribute("expiredUnits", inventoryService.getExpiredUnitsCount());

        // Stock Chart Data
        Map<String, Integer> stockOverview = inventoryService.getStockOverviewMap();
        model.addAttribute("chartKeys", stockOverview.keySet());
        model.addAttribute("chartValues", stockOverview.values());

        // Recent Audit Log
        model.addAttribute("recentLogs", activityLogRepository.findAllByOrderByTimestampDesc(PageRequest.of(0, 5)).getContent());

        return "dashboard/admin";
    }

    @GetMapping("/staff/dashboard")
    public String staffDashboard(Model model) {
        model.addAttribute("availableUnits", inventoryService.getTotalAvailableUnits());
        model.addAttribute("pendingRequests", requestService.countPendingRequests());
        model.addAttribute("totalDonations", donationService.countTotalDonations());

        // Stock Overview
        Map<String, Integer> stockOverview = inventoryService.getStockOverviewMap();
        model.addAttribute("stockOverview", stockOverview);

        // Recent Donations & Requests
        model.addAttribute("recentDonations", donationService.findRecentDonations(5));
        model.addAttribute("recentRequests", requestService.findRecentRequests(5));

        return "dashboard/staff";
    }

    @GetMapping("/donor/dashboard")
    public String donorDashboard(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Donor donor = donorService.findByUser(user).orElse(null);
        if (donor == null) {
            model.addAttribute("error", "No donor profile found for your user account.");
            return "dashboard/donor";
        }

        model.addAttribute("donor", donor);

        // Recalculate status and eligibility checks
        donorService.updateEligibilityStatus(donor);
        boolean isEligible = donorService.isEligibleForDonation(donor);
        model.addAttribute("isEligible", isEligible);

        if (!isEligible) {
            long daysLeft = donorService.getDaysUntilEligible(donor);
            model.addAttribute("daysUntilEligible", daysLeft);
            if (donor.getLastDonationDate() != null) {
                model.addAttribute("nextEligibleDate", donor.getLastDonationDate().plusDays(90));
            }
        }

        // Donation History
        List<BloodDonation> history = donationService.findByDonor(donor);
        model.addAttribute("donations", history);

        // Standard Blood donation reminder message
        if (isEligible) {
            model.addAttribute("reminderMessage", "You are eligible to donate blood today! Click below to book an appointment.");
        } else {
            model.addAttribute("reminderMessage", "Thank you for your service. You will be eligible to donate again after your deferral window completes.");
        }

        return "dashboard/donor";
    }
}
