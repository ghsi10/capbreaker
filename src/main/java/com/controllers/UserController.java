package com.controllers;

import com.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
public class UserController {

    @Value("${agent.server.dns}")
    private String SERVER_DNS;
    @Value("${agent.download.url}")
    private String DOWNLOAD_URL;

    private UserService userService;

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
    public String signup(@RequestParam String username, @RequestParam String password, @RequestParam String
            passwordAgain) throws NoSuchFieldException {
        if (username.length() < 4 || password.length() > 17)
            throw new NoSuchFieldException("Username/Password should be between 4 to 16");
        if (!username.matches("[a-zA-Z][a-zA-Z0-9]+") || !password.matches("[a-zA-Z0-9]+"))
            throw new NoSuchFieldException("Username/Password contains illegal characters");
        if (!password.equals(passwordAgain))
            throw new NoSuchFieldException("Password does not match the confirm password");
        userService.signup(username, password);
        return "redirect:/tasks";
    }

    @GetMapping("user/download")
    public String download(Model model, HttpServletResponse response, @AuthenticationPrincipal User user) {
        response.setHeader("Content-Disposition", "attachment; filename=\"CapBreakerAgent.py\"");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("password", userService.getPassword(user.getUsername()));
        model.addAttribute("server", SERVER_DNS);
        model.addAttribute("url", DOWNLOAD_URL);
        return "user/agent";
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
    public String adminUsersManagement(Model model) {
        model.addAttribute("module", "usersManagement");
        model.addAttribute("users", userService.getUsers());
        return "user/users-management";
    }

    @GetMapping("/admin/enabledUser")
    public String adminEnabledUser(@RequestParam int userId, @RequestParam boolean enabled) {
        userService.enabledUser(userId, enabled);
        return "redirect:/admin/users-management";
    }

    @GetMapping("/admin/deleteUser")
    public String adminDeleteUser(@RequestParam int userId) {
        userService.deleteUser(userId);
        return "redirect:/admin/users-management";
    }

    @GetMapping("/admin/promoteUser")
    public String adminPromoteUser(@RequestParam int userId, @RequestParam boolean promote) {
        userService.promoteUser(userId, promote);
        return "redirect:/admin/users-management";
    }

    @ExceptionHandler(NumberFormatException.class)
    public String handleNumberFormatException(Model model) {
        model.addAttribute("module", "result");
        model.addAttribute("error", "Invalid task id.");
        return "result";
    }

    @ExceptionHandler(NoSuchFieldException.class)
    public String handleNoSuchFieldException(NoSuchFieldException e, Model model) {
        model.addAttribute("module", "signup");
        model.addAttribute("error", e.getMessage());
        return "user/signup";
    }

    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }
}
