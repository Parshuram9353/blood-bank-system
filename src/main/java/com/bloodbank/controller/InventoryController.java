package com.bloodbank.controller;

import com.bloodbank.entity.BloodInventory;
import com.bloodbank.service.DonorService;
import com.bloodbank.service.InventoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final DonorService donorService;

    @GetMapping
    public String listInventory(
            @RequestParam(value = "search", required = false) String search,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            Model model) {
        
        Page<BloodInventory> inventoryPage = inventoryService.search(search, page, size);
        model.addAttribute("inventoryList", inventoryPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", inventoryPage.getTotalPages());
        model.addAttribute("totalItems", inventoryPage.getTotalElements());
        model.addAttribute("search", search);
        
        // Also send total counts per group
        model.addAttribute("stockMap", inventoryService.getStockOverviewMap());
        model.addAttribute("totalAvailable", inventoryService.getTotalAvailableUnits());

        return "inventory/list";
    }

    @GetMapping("/new")
    public String showAddForm(Model model) {
        model.addAttribute("inventory", new BloodInventory());
        model.addAttribute("donors", donorService.findAllList());
        return "inventory/form";
    }

    @PostMapping("/new")
    public String saveInventory(
            @Valid @ModelAttribute("inventory") BloodInventory inventory,
            BindingResult bindingResult,
            Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("donors", donorService.findAllList());
            return "inventory/form";
        }
        
        inventoryService.save(inventory);
        return "redirect:/inventory?success=added";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable("id") Long id, Model model) {
        BloodInventory inventory = inventoryService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid inventory Id: " + id));
        model.addAttribute("inventory", inventory);
        model.addAttribute("donors", donorService.findAllList());
        return "inventory/form";
    }

    @PostMapping("/edit/{id}")
    public String updateInventory(
            @PathVariable("id") Long id,
            @Valid @ModelAttribute("inventory") BloodInventory inventory,
            BindingResult bindingResult,
            Model model) {
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("donors", donorService.findAllList());
            return "inventory/form";
        }
        
        inventory.setId(id);
        inventoryService.save(inventory);
        return "redirect:/inventory?success=updated";
    }

    @GetMapping("/delete/{id}")
    public String deleteInventory(@PathVariable("id") Long id) {
        inventoryService.deleteById(id);
        return "redirect:/inventory?success=deleted";
    }
}
