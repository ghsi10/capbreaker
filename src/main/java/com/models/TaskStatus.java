package com.models;

public enum TaskStatus {
	Queued("Queued"), Working("Working"), Completed("Completed");

	private String status;

	TaskStatus(String status) {
		this.status = status;
	}

	public static TaskStatus fromString(String text) {
		for (TaskStatus taskStatus : TaskStatus.values()) {
			if (taskStatus.toString().equalsIgnoreCase(text)) {
				return taskStatus;
			}
		}
		return null;
	}

	@Override
	public String toString() {
		return status;
	}
}
