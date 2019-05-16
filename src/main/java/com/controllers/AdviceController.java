package com.controllers;

import com.services.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdviceController {

    private final AgentService agentService;

    @Autowired
    public AdviceController(AgentService agentService) {
        this.agentService = agentService;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("agents", "Scanning agents: " + agentService.agentCounter());
    }

    @ExceptionHandler(NumberFormatException.class)
    public String handleNumberFormatException(Model model) {
        model.addAttribute("module", "result");
        model.addAttribute("error", "Invalid task id.");
        return "result";
    }
}
