package com.services;

import java.io.IOException;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dao.TaskDao;
import com.models.Handshake;
import com.models.Task;

@Service
public class TaskService {

	@Autowired
	private TaskDao taskDao;
	@Autowired
	private HashConvert hashConvert;

	public List<Task> getTable() {
		return taskDao.findTop50ByOrderByIdDesc();
	}

	public Task uploadCap(byte[] file, String essid, String bssid) throws IOException {
		Handshake handshake = hashConvert.convert(file, essid, bssid);
		Task task = new Task(handshake);
		taskDao.save(task);
		return task;
	}

	public Task getResult(String taskId, String taskPassword) {
		int taskid = -1;
		try {
			taskid = Integer.parseInt(taskId);
		} catch (NumberFormatException e) {
			return null;
		}
		Task task = taskDao.findOne(taskid);
		if (task != null && task.getTaskPassword().equals(taskPassword))
			return task;
		return null;
	}
}
