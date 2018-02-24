package com.models;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class ScanTask {

	private Task task;
	private Map<String, String[]> scanCommands;

	public ScanTask() {
		this.scanCommands = new HashMap<>();
	}

	public ScanTask(Task task) {
		this.task = task;
		this.scanCommands = new HashMap<>();
	}

	public ScanTask(Task task, String uuid, String[] command) {
		this.task = task;
		this.scanCommands = new HashMap<>();
		addCommand(uuid, command);
	}

	public ScanTask(Task task, Map<String, String[]> scanCommands) {
		this.task = task;
		this.scanCommands = scanCommands;
	}

	public void addCommand(String uuid, String[] command) {
		scanCommands.put(uuid, command);
	}

	public Entry<String, String[]> pollCommand() {
		if (scanCommands.isEmpty())
			return null;
		Entry<String, String[]> first = scanCommands.entrySet().iterator().next();
		scanCommands.remove(first.getKey());
		return first;
	}

	public boolean isEmpty() {
		return scanCommands.isEmpty();
	}

	public Task getTask() {
		return task;
	}

	public void setTask(Task task) {
		this.task = task;
	}

	public Map<String, String[]> getScanCommands() {
		return scanCommands;
	}

	public void setScanCommands(Map<String, String[]> scanCommands) {
		this.scanCommands = scanCommands;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ScanTask) {
			ScanTask scanTask = (ScanTask) obj;
			return scanTask.task.equals(task) && scanTask.scanCommands.equals(scanCommands);
		}
		return false;
	}

	@Override
	public String toString() {
		return "ScanTask [task=" + task + ", scanCommands=" + scanCommands + "]";
	}

}
