package com.controllers;

import java.io.IOException;

import javax.activation.UnsupportedDataTypeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.models.Task;
import com.services.TaskService;

@Controller
public class TaskController {
	
	private TaskService taskService;
	
	@RequestMapping(value = { "/", "/tasks" }, method = RequestMethod.GET)
	public String getTable(Model model) {
		model.addAttribute("module", "tasks");
		model.addAttribute("tasks", taskService.getTable());
		return "tasks";
	}

	@RequestMapping(value = "/upload", method = RequestMethod.GET)
	public String uploaded(Model model) {
		model.addAttribute("module", "upload");
		return "upload";
	}

	@RequestMapping(value = "/upload", method = RequestMethod.POST)
	public String uploaded(Model model, @RequestParam MultipartFile capFile, @RequestParam String essid,
			@RequestParam String bssid) throws UnsupportedDataTypeException, IOException {
		model.addAttribute("module", "upload");
		Task task = taskService.uploadCap(capFile.getBytes(), essid, bssid);
		model.addAttribute("task", task);
		return "uploaded";
	}

	@RequestMapping(value = "/result", method = RequestMethod.GET)
	public String result(Model model) {
		model.addAttribute("module", "result");
		return "result";
	}

	@RequestMapping(value = "/result", method = RequestMethod.POST)
	public String resultOf(Model model, @RequestParam String taskId, @RequestParam String taskPassword) {
		model.addAttribute("module", "result");
		model.addAttribute("task", taskService.getResult(taskId, taskPassword));
		return "resultof";
	}

	@ExceptionHandler(UnsupportedDataTypeException.class)
	public String handleDbError(UnsupportedDataTypeException e, Model model) {
		model.addAttribute("module", "upload");
		model.addAttribute("error", e.getMessage());
		return "uploaded";
	}

	@ExceptionHandler(IOException.class)
	public String handleDbError(IOException e, Model model) {
		model.addAttribute("module", "upload");
		model.addAttribute("error", "Unexpected error");
		return "uploaded";
	}

	@ExceptionHandler(NumberFormatException.class)
	public String handleDbError(NumberFormatException e, Model model) {
		model.addAttribute("module", "result");
		model.addAttribute("task", null);
		return "resultof";
	}

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}
}
