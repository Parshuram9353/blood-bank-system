package com.bloodbank.controller;

import com.bloodbank.dto.DonorRegistrationDto;
import com.bloodbank.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("donorDto", new DonorRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerDonor(
            @Valid @ModelAttribute("donorDto") DonorRegistrationDto donorDto,
            BindingResult bindingResult,
            Model model) {

        if (userService.existsByUsername(donorDto.getUsername())) {
            bindingResult.rejectValue("username", "error.username", "Username is already taken");
        }

        if (userService.existsByEmail(donorDto.getEmail())) {
            bindingResult.rejectValue("email", "error.email", "Email is already registered");
        }

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            userService.registerDonor(donorDto);
            return "redirect:/login?registered=true";
        } catch (Exception e) {
            log.error("Error registering donor", e);
            model.addAttribute("errorMessage", "An error occurred during registration. Please try again.");
            return "register";
        }
    }
}
