package com.controllers;

import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class UserController {

	@RequestMapping(value = "/login", method = RequestMethod.GET)
	public String login(Model model) {
		model.addAttribute("module", "login");
		return "user/login";
	}

	@RequestMapping(value = { "user/download" }, method = RequestMethod.GET)
	public String download(Model model, HttpServletResponse response, @AuthenticationPrincipal User user) {
		response.setHeader("Content-Disposition", "attachment; filename=\"CapBreakerAgent.py\"");
		model.addAttribute("username", user.getUsername());
		model.addAttribute("password", user.getPassword());
		return "user/agent";
	}
}
