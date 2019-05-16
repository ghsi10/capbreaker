package com.controllers;

import com.models.ScanCommand;
import com.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserService userService;

    @Autowired
    public AdminController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/scans")
    public String scans(Model model) {
        model.addAttribute("module", "scans");
        model.addAttribute("commands", userService.getCommands());
        return "user/scans";
    }

    @PostMapping("/scans")
    public String resetCommands() {
        userService.resetCommands();
        return "redirect:/admin/scans";
    }

    @GetMapping("/command/delete/{id}")
    public String deleteCommand(@PathVariable int id) {
        userService.deleteCommand(id);
        return "redirect:/admin/scans";
    }

    @GetMapping("/command/edit/{id}")
    public String saveCommandPage(Model model, @PathVariable int id) {
        model.addAttribute("module", "scans");
        model.addAttribute("command", userService.getCommand(id));
        return "user/command";
    }

    @PostMapping("/command/edit/{id}")
    public String saveCommand(@PathVariable Integer id, @RequestParam Integer priority, @RequestPart String command) {
        id = id == ScanCommand.NO_ID ? null : id;
        priority = priority == null ? 0 : priority;
        userService.saveCommand(new ScanCommand(id, priority, command));
        return "redirect:/admin/scans";
    }

    @GetMapping("/task/result")
    public String taskResult(Model model, @RequestParam String taskId) {
        model.addAttribute("module", "result");
        model.addAttribute("task", userService.taskResult(taskId));
        return "resultof";
    }

    @GetMapping("/task/delete/{id}")
    public String deleteTask(@PathVariable int id) {
        userService.deleteTask(id);
        return "redirect:/tasks";
    }

    @GetMapping("/users")
    public String usersManagement(Model model) {
        model.addAttribute("module", "users");
        model.addAttribute("users", userService.getUsers());
        return "user/users";
    }

    @GetMapping("/user/enabled/{id}")
    public String enabledUser(@PathVariable int id, @RequestParam boolean enabled) {
        userService.enabledUser(id, enabled);
        return "redirect:/admin/users";
    }

    @GetMapping("/user/delete/{id}")
    public String deleteUser(@PathVariable int id) {
        userService.deleteUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/user/promote/{id}")
    public String promoteUser(@PathVariable int id, @RequestParam boolean promote) {
        userService.promoteUser(id, promote);
        return "redirect:/admin/users";
    }
}
