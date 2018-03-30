package com.controllers;

import com.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;

@Controller
public class UserController {

    @Value("${agent.server.dns}")
    private String SERVER_DNS;
    @Value("${agent.download.url}")
    private String DOWNLOAD_URL;

    private UserService userService;

    @RequestMapping(value = "/signin", method = RequestMethod.GET)
    public String signin(Model model) {
        model.addAttribute("module", "signin");
        return "user/signin";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.GET)
    public String signup(Model model) {
        model.addAttribute("module", "signup");
        return "user/signup";
    }

    @RequestMapping(value = "/signup", method = RequestMethod.POST)
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

    @RequestMapping(value = {"user/download"}, method = RequestMethod.GET)
    public String download(Model model, HttpServletResponse response, @AuthenticationPrincipal User user) {
        response.setHeader("Content-Disposition", "attachment; filename=\"CapBreakerAgent.py\"");
        model.addAttribute("username", user.getUsername());
        model.addAttribute("password", userService.getPassword(user.getUsername()));
        model.addAttribute("server", SERVER_DNS);
        model.addAttribute("url", DOWNLOAD_URL);
        return "user/agent";
    }

    @RequestMapping(value = "/admin/result", method = RequestMethod.GET)
    public String adminResult(Model model, @RequestParam String taskId) {
        model.addAttribute("module", "result");
        model.addAttribute("task", userService.getResult(taskId));
        return "resultof";
    }

    @RequestMapping(value = "/admin/delete", method = RequestMethod.GET)
    public String adminDelete(@RequestParam String taskId) {
        userService.deleteTask(taskId);
        return "redirect:/tasks";
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
