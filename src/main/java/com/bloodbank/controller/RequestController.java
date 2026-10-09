package com.bloodbank.controller;

import com.bloodbank.entity.BloodRequest;
import com.bloodbank.repository.HospitalRepository;
import com.bloodbank.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {

    private final RequestService requestService;
    private final HospitalRepository hospitalRepository;

    @GetMapping
    public String listRequests(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        Page<BloodRequest> requestPage = requestService.search(search, page, size);
        model.addAttribute("requests", requestPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", requestPage.getTotalPages());
        model.addAttribute("totalItems", requestPage.getTotalElements());
        model.addAttribute("search", search);
        return "requests/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("bloodRequest", new BloodRequest());
        model.addAttribute("hospitals", hospitalRepository.findAll());
        return "requests/form";
    }

    @PostMapping("/new")
    public String createRequest(
            @Valid @ModelAttribute("bloodRequest") BloodRequest bloodRequest,
            BindingResult bindingResult,
            Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("hospitals", hospitalRepository.findAll());
            return "requests/form";
        }
        
        requestService.createRequest(bloodRequest);
        return "redirect:/requests?success=created";
    }

    @GetMapping("/view/{id}")
    public String viewRequest(@PathVariable("id") Long id, Model model) {
        BloodRequest bloodRequest = requestService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid request Id: " + id));
        model.addAttribute("bloodRequest", bloodRequest);
        return "requests/detail";
    }

    @PostMapping("/approve/{id}")
    public String approveRequest(@PathVariable("id") Long id, Model model) {
        try {
            requestService.approveAndFulfill(id);
            return "redirect:/requests/view/" + id + "?success=approved";
        } catch (Exception e) {
            log.error("Error approving request", e);
            return "redirect:/requests/view/" + id + "?error=" + e.getMessage();
        }
    }

    @PostMapping("/reject/{id}")
    public String rejectRequest(
            @PathVariable("id") Long id,
            @RequestParam("reason") String reason) {
        
        try {
            requestService.rejectRequest(id, reason);
            return "redirect:/requests/view/" + id + "?success=rejected";
        } catch (Exception e) {
            log.error("Error rejecting request", e);
            return "redirect:/requests/view/" + id + "?error=" + e.getMessage();
        }
    }
}
