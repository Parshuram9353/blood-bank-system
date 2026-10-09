package com.bloodbank.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgument(IllegalArgumentException ex, Model model) {
        log.error("Illegal Argument Exception intercept: ", ex);
        model.addAttribute("errorTitle", "Invalid Action");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(IllegalStateException.class)
    public String handleIllegalState(IllegalStateException ex, Model model) {
        log.error("Illegal State Exception intercept: ", ex);
        model.addAttribute("errorTitle", "Inventory/Workflow Conflict");
        model.addAttribute("errorMessage", ex.getMessage());
        return "error";
    }

    @ExceptionHandler(Exception.class)
    public String handleGeneral(Exception ex, Model model) {
        log.error("Unhandled Exception intercept: ", ex);
        model.addAttribute("errorTitle", "Internal System Error");
        model.addAttribute("errorMessage", "Something went wrong. Technical details: " + ex.getMessage());
        return "error";
    }
}
