package com.models;

import java.util.LinkedList;
import java.util.List;

import org.springframework.data.util.Pair;

public class ScanTask {

	private Task task;
	private List<Pair<String, String[]>> scanCommands;

	public ScanTask(Task task) {
		this.task = task;
		scanCommands = new LinkedList<>();
	}

	public ScanTask(ScanTask scanTask) {
		task = scanTask.task;
		scanCommands = scanTask.scanCommands;
	}

	public ScanTask(Task task, String uuid, String[] command) {
		this.task = task;
		scanCommands = new LinkedList<>();
		scanCommands.add(Pair.of(uuid, command));
	}

	public void addCommand(String uuid, String[] command) {
		scanCommands.add(Pair.of(uuid, command));
	}

	public Pair<String, String[]> pollCommand() {
		return scanCommands.remove(0);
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

	public List<Pair<String, String[]>> getScanCommands() {
		return scanCommands;
	}

	public void setScanCommands(List<Pair<String, String[]>> scanCommands) {
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
