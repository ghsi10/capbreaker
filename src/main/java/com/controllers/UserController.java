package com.controllers;

import com.exceptions.ValidationException;
import com.models.ScanCommand;
import com.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class UserController {

    @Value("${agent.server.dns}")
    private String serverDns;
    @Value("${agent.download.url}")
    private String downloadUrl;

    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/signin")
    public String signin(Model model, @RequestParam(required = false) String error, @RequestParam(required = false)
            String logout, HttpServletRequest request) {
        model.addAttribute("module", "signin");
        if (error != null) {
            error = "Invalid username and password.";
            if (request.getSession().getAttribute("SPRING_SECURITY_LAST_EXCEPTION") instanceof DisabledException)
                error = "This user is pending to admin approval.";
            model.addAttribute("error", error);
        }
        if (logout != null)
            model.addAttribute("msg", "You have been logged out.");
        return "user/signin";
    }

    @GetMapping("/signup")
    public String signup(Model model) {
        model.addAttribute("module", "signup");
        return "user/signup";
    }

    @PostMapping("/signup")
    public String signup(@RequestParam String username, @RequestParam String password,
                         @RequestParam String passwordAgain) throws ValidationException {
        if (username.length() < 4 || password.length() > 17)
            throw new ValidationException("Username/Password should be between 4 to 16");
        if (!username.matches("[a-zA-Z][a-zA-Z0-9]+") || !password.matches("[a-zA-Z0-9]+"))
            throw new ValidationException("Username/Password contains illegal characters");
        if (!password.equals(passwordAgain))
            throw new ValidationException("Password does not match the confirm password");
        userService.signup(username, password);
        return "redirect:/tasks";
    }

    @GetMapping("user/download")
    public String download(Model model, HttpServletResponse response, @AuthenticationPrincipal User user) {
        response.setHeader("Content-Disposition", "attachment; filename=\"CapBreakerAgent.py\"");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("password", userService.getPassword(user.getUsername()));
        model.addAttribute("server", serverDns);
        model.addAttribute("url", downloadUrl);
        return "user/agent";
    }

    @GetMapping("/admin/scans-management")
    public String scansManagement(Model model) {
        model.addAttribute("module", "scansManagement");
        model.addAttribute("commands", userService.getCommands());
        return "user/scans-management";
    }

    @PostMapping("/admin/scans-management")
    public String resetCommands() {
        userService.resetCommands();
        return "redirect:/admin/scans-management";
    }

    @GetMapping("/admin/deleteCommand")
    public String deleteCommand(@RequestParam int commandId) {
        userService.deleteCommand(commandId);
        return "redirect:/admin/scans-management";
    }

    @GetMapping("admin/editCommand")
    public String saveCommandPage(Model model, @RequestParam int commandId) {
        model.addAttribute("module", "scansManagement");
        model.addAttribute("command", userService.getCommand(commandId));
        return "user/command";
    }

    @PostMapping("/admin/editCommand")
    public String saveCommand(@RequestParam Integer commandId, @RequestParam int priority, @RequestPart String command) {
        commandId = commandId == ScanCommand.NO_ID ? null : commandId;
        userService.saveCommand(new ScanCommand(commandId, priority, command));
        return "redirect:/admin/scans-management";
    }

    @GetMapping("/admin/taskResult")
    public String taskResult(Model model, @RequestParam String taskId) {
        model.addAttribute("module", "result");
        model.addAttribute("task", userService.taskResult(taskId));
        return "resultof";
    }

    @GetMapping("/admin/deleteTask")
    public String deleteTask(@RequestParam int taskId) {
        userService.deleteTask(taskId);
        return "redirect:/tasks";
    }

    @GetMapping("admin/users-management")
    public String usersManagement(Model model) {
        model.addAttribute("module", "usersManagement");
        model.addAttribute("users", userService.getUsers());
        return "user/users-management";
    }

    @GetMapping("/admin/enabledUser")
    public String enabledUser(@RequestParam int userId, @RequestParam boolean enabled) {
        userService.enabledUser(userId, enabled);
        return "redirect:/admin/users-management";
    }

    @GetMapping("/admin/deleteUser")
    public String deleteUser(@RequestParam int userId) {
        userService.deleteUser(userId);
        return "redirect:/admin/users-management";
    }

    @GetMapping("/admin/promoteUser")
    public String promoteUser(@RequestParam int userId, @RequestParam boolean promote) {
        userService.promoteUser(userId, promote);
        return "redirect:/admin/users-management";
    }

    @ExceptionHandler(NumberFormatException.class)
    public String handleNumberFormatException(Model model) {
        model.addAttribute("module", "result");
        model.addAttribute("error", "Invalid task id.");
        return "result";
    }

    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(ValidationException e, Model model) {
        model.addAttribute("module", "signup");
        model.addAttribute("error", e.getMessage());
        return "user/signup";
    }
}
