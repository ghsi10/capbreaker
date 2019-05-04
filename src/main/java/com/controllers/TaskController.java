package com.controllers;

import com.exceptions.UnsupportedDataTypeException;
import com.services.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Controller
public class TaskController {

    private final TaskService taskService;

    @Autowired
    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @GetMapping({"/", "/tasks"})
    public String getTable(Model model, @RequestParam(required = false, defaultValue = "0") int page) {
        model.addAttribute("module", "tasks");
        model.addAttribute("tasks", taskService.getTable(page));
        return "tasks";
    }

    @GetMapping("/upload-file")
    public String uploadFile(Model model) {
        model.addAttribute("module", "upload");
        return "upload-file";
    }

    @GetMapping("/upload-text")
    public String uploadText(Model model) {
        model.addAttribute("module", "upload");
        return "upload-text";
    }

    @PostMapping("/upload-file")
    public String uploaded(Model model, @RequestParam MultipartFile capFile, @RequestParam String essid,
                           @RequestParam String bssid) throws IOException {
        model.addAttribute("module", "upload");
        model.addAttribute("task", taskService.uploadFile(capFile.getBytes(), essid, bssid));
        return "uploaded";
    }

    @PostMapping("/upload-text")
    public String uploaded(Model model, @RequestParam String pmkid) throws IOException {
        model.addAttribute("module", "upload");
        model.addAttribute("task", taskService.uploadText(pmkid));
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
        return e.getFrom();
    }

    @ExceptionHandler({IOException.class, ArrayIndexOutOfBoundsException.class})
    public String handleIOException(Model model) {
        model.addAttribute("module", "upload");
        model.addAttribute("error", "Unexpected error");
        return "upload-file";
    }

    @ExceptionHandler(NumberFormatException.class)
    public String handleNumberFormatException(Model model) {
        model.addAttribute("module", "result");
        model.addAttribute("error", "Invalid task id and password.");
        return "result";
    }
}
