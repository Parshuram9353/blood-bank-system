package com.bloodbank.controller;

import com.bloodbank.entity.BloodDonation;
import com.bloodbank.entity.Donor;
import com.bloodbank.entity.User;
import com.bloodbank.service.DonationService;
import com.bloodbank.service.DonorService;
import com.bloodbank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@Slf4j
public class DonationController {

    private final DonationService donationService;
    private final DonorService donorService;
    private final UserService userService;

    // ==========================================
    // STAFF & ADMIN DONATION MANAGEMENT
    // ==========================================

    @GetMapping("/donations")
    public String listDonations(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        Page<BloodDonation> donationPage = donationService.search(search, page, size);
        model.addAttribute("donations", donationPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", donationPage.getTotalPages());
        model.addAttribute("totalItems", donationPage.getTotalElements());
        model.addAttribute("search", search);
        return "donations/list";
    }

    @GetMapping("/donations/new")
    public String showRegisterForm(Model model) {
        model.addAttribute("donation", new BloodDonation());
        model.addAttribute("donors", donorService.findAllList());
        return "donations/register";
    }

    @PostMapping("/donations/new")
    public String registerDonation(@Valid @ModelAttribute("donation") BloodDonation donation, BindingResult bindingResult, Model model) {
        if (bindingResult.hasErrors()) {
            model.addAttribute("donors", donorService.findAllList());
            return "donations/register";
        }
        
        try {
            donation.setDonationDate(LocalDate.now());
            donation.setStatus("PENDING");
            donationService.registerDonation(donation);
            return "redirect:/donations?success=registered";
        } catch (Exception e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("donors", donorService.findAllList());
            return "donations/register";
        }
    }

    @PostMapping("/donations/finalize/{id}")
    public String finalizeDonation(
            @PathVariable("id") Long id,
            @RequestParam("status") String status,
            @RequestParam("remarks") String remarks) {
        
        try {
            donationService.completeDonation(id, status, remarks);
            return "redirect:/donations?success=finalized";
        } catch (Exception e) {
            log.error("Error finalizing donation", e);
            return "redirect:/donations?error=" + e.getMessage();
        }
    }

    // ==========================================
    // DONOR APPOINTMENT BOOKING
    // ==========================================

    @GetMapping("/donor/book")
    public String showBookingForm(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElse(null);
        if (user == null) {
            return "redirect:/login";
        }

        Donor donor = donorService.findByUser(user).orElse(null);
        if (donor == null) {
            model.addAttribute("error", "No donor profile found.");
            return "dashboard/donor";
        }

        // Check if eligible
        donorService.updateEligibilityStatus(donor);
        if (!donorService.isEligibleForDonation(donor)) {
            model.addAttribute("eligibilityError", "You are not eligible to book an appointment at this time.");
            model.addAttribute("daysLeft", donorService.getDaysUntilEligible(donor));
            return "dashboard/donor";
        }

        BloodDonation appointment = new BloodDonation();
        appointment.setDonor(donor);
        appointment.setBloodGroup(donor.getBloodGroup());
        appointment.setDonationDate(LocalDate.now().plusDays(1)); // Default tomorrow

        model.addAttribute("appointment", appointment);
        model.addAttribute("donor", donor);
        return "donations/book";
    }

    @PostMapping("/donor/book")
    public String bookAppointment(@Valid @ModelAttribute("appointment") BloodDonation appointment, BindingResult bindingResult, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(auth.getName()).orElse(null);
        Donor donor = donorService.findByUser(user).orElseThrow();

        if (bindingResult.hasErrors()) {
            model.addAttribute("donor", donor);
            return "donations/book";
        }

        if (appointment.getDonationDate().isBefore(LocalDate.now())) {
            bindingResult.rejectValue("donationDate", "error.date", "Appointment date cannot be in the past");
            model.addAttribute("donor", donor);
            return "donations/book";
        }

        appointment.setDonor(donor);
        appointment.setBloodGroup(donor.getBloodGroup());
        appointment.setStatus("PENDING");
        appointment.setUnits(1); // Standard donation
        
        donationService.registerDonation(appointment);
        return "redirect:/dashboard?success=booked";
    }
}
