package com.bloodbank.controller;

import com.bloodbank.entity.Donor;
import com.bloodbank.service.DonationService;
import com.bloodbank.service.DonorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/donors")
@RequiredArgsConstructor
public class DonorController {

    private final DonorService donorService;
    private final DonationService donationService;

    @GetMapping
    public String listDonors(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        Page<Donor> donorPage = donorService.search(search, page, size);
        model.addAttribute("donors", donorPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", donorPage.getTotalPages());
        model.addAttribute("totalItems", donorPage.getTotalElements());
        model.addAttribute("search", search);
        return "donors/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("donor", new Donor());
        return "donors/form";
    }

    @PostMapping("/new")
    public String saveDonor(@Valid @ModelAttribute("donor") Donor donor, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "donors/form";
        }
        
        donorService.save(donor);
        return "redirect:/donors?success=added";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        Donor donor = donorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid donor Id: " + id));
        model.addAttribute("donor", donor);
        return "donors/form";
    }

    @PostMapping("/edit/{id}")
    public String updateDonor(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("donor") Donor donor,
            BindingResult bindingResult) {
        
        if (bindingResult.hasErrors()) {
            return "donors/form";
        }
        
        donor.setId(id);
        donorService.save(donor);
        return "redirect:/donors?success=updated";
    }

    @GetMapping("/view/{id}")
    public String viewDonor(@PathVariable("id") Long id, Model model) {
        Donor donor = donorService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid donor Id: " + id));
        model.addAttribute("donor", donor);
        
        donorService.updateEligibilityStatus(donor);
        model.addAttribute("isEligible", donorService.isEligibleForDonation(donor));
        model.addAttribute("daysUntilEligible", donorService.getDaysUntilEligible(donor));
        model.addAttribute("donations", donationService.findByDonor(donor));
        
        return "donors/view";
    }

    @GetMapping("/delete/{id}")
    public String deleteDonor(@PathVariable("id") Long id) {
        donorService.deleteById(id);
        return "redirect:/donors?success=deleted";
    }
}
