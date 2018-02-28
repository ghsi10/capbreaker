package com.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.models.Task;
import com.repositories.TaskRepository;

@Service
public class UserService {

	private TaskRepository taskRepository;
	private ScanManager scanManager;

	public Task getResult(String taskId) throws NumberFormatException {
		Task task = taskRepository.findOne(Integer.parseInt(taskId));
		if (task != null)
			return task;
		throw new NumberFormatException();
	}

	public void deletTask(String taskId) {
		scanManager.stopTask(Integer.parseInt(taskId));
		taskRepository.delete(Integer.parseInt(taskId));
	}

	@Autowired
	public void setTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Autowired
	public void setScanManager(ScanManager scanManager) {
		this.scanManager = scanManager;
	}
}
