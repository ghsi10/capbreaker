package com.controllers;

import com.services.ScanManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class AdviceController {

    private final ScanManager scanManager;

    @Autowired
    public AdviceController(ScanManager scanManager) {
        this.scanManager = scanManager;
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("agents", "Online agents: " + scanManager.agentCounter());
    }
}
