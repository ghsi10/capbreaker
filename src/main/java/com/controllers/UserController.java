package com.controllers;

import com.exceptions.ValidationException;
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

    @ExceptionHandler(ValidationException.class)
    public String handleValidationException(ValidationException e, Model model) {
        model.addAttribute("module", "signup");
        model.addAttribute("error", e.getMessage());
        return "user/signup";
    }
}
