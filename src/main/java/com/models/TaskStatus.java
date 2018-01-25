package com.models;

public enum TaskStatus {
	Queued("Queued"), Working("Working"), Completed("Completed");

	private String status;

	TaskStatus(String status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return status;
	}
}
