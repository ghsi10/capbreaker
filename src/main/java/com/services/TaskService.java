package com.services;

import java.util.List;

import javax.activation.UnsupportedDataTypeException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.models.Handshake;
import com.models.Task;
import com.repositories.TaskRepository;

@Service
public class TaskService {

	@Value("${view.page.size}")
	private int PAGE_SIZE;

	private TaskRepository taskRepository;
	private HashConvert hashConvert;

	public List<Task> getTable(int page) {
		return taskRepository.findAllByOrderByIdDesc(new PageRequest(page, PAGE_SIZE)).getContent();
	}

	public Task uploadCap(byte[] file, String essid, String bssid) throws UnsupportedDataTypeException {
		Handshake handshake = hashConvert.convert(file, essid, bssid);
		Task task = new Task(handshake);
		taskRepository.save(task);
		return task;
	}

	public Task getResult(String taskId, String taskPassword) throws NumberFormatException {
		Task task = taskRepository.findOne(Integer.parseInt(taskId));
		if (task != null && task.getTaskPassword().equals(taskPassword))
			return task;
		throw new NumberFormatException();
	}

	@Autowired
	public void setTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	@Autowired
	public void setHashConvert(HashConvert hashConvert) {
		this.hashConvert = hashConvert;
	}
}
