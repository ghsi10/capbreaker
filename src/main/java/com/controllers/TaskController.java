package com.controllers;

import com.services.ScanManager;
import com.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.UnsupportedDataTypeException;
import java.io.IOException;

@Controller
public class TaskController {

    private TaskService taskService;
    private ScanManager scanManager;

    @GetMapping({"/", "/tasks"})
    public String getTable(Model model, @RequestParam(required = false, defaultValue = "0") int page) {
        model.addAttribute("module", "tasks");
        model.addAttribute("tasks", taskService.getTable(page));
        return "tasks";
    }

    @GetMapping("/upload")
    public String uploaded(Model model) {
        model.addAttribute("module", "upload");
        return "upload";
    }

    @PostMapping("/upload")
    public String uploaded(Model model, @RequestParam MultipartFile capFile, @RequestParam String essid,
                           @RequestParam String bssid) throws IOException {
        model.addAttribute("module", "upload");
        model.addAttribute("task", taskService.uploadCap(capFile.getBytes(), essid, bssid));
        return "uploaded";
    }

    @GetMapping("/result")
    public String result(Model model) {
        model.addAttribute("module", "result");
        return "result";
    }

    @PostMapping("/result")
    public String resultOf(Model model, @RequestParam String taskId, @RequestParam String taskPassword) throws
            NumberFormatException {
        model.addAttribute("module", "result");
        model.addAttribute("task", taskService.getResult(taskId, taskPassword));
        return "resultof";
    }

    @ExceptionHandler(UnsupportedDataTypeException.class)
    public String handleUnsupportedDataTypeException(UnsupportedDataTypeException e, Model model) {
        model.addAttribute("module", "upload");
        model.addAttribute("error", e.getMessage());
        return "upload";
    }

    @ExceptionHandler({IOException.class, ArrayIndexOutOfBoundsException.class})
    public String handleIOException(Model model) {
        model.addAttribute("module", "upload");
        model.addAttribute("error", "Unexpected error");
        return "upload";
    }

    @ExceptionHandler(NumberFormatException.class)
    public String handleNumberFormatException(Model model) {
        model.addAttribute("module", "result");
        model.addAttribute("error", "Invalid task id and password.");
        return "result";
    }

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("agents", "Online agents: " + scanManager.agentCounter());
    }

    @Autowired
    public void setTaskService(TaskService taskService) {
        this.taskService = taskService;
    }

    @Autowired
    public void setScanManager(ScanManager scanManager) {
        this.scanManager = scanManager;
    }

}
