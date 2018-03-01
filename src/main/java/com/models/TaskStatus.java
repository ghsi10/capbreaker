package com.models;

public enum TaskStatus {
	Queued("Queued"), Working("Working"), Completed("Completed");

	private final String status;

	TaskStatus(String status) {
		this.status = status;
	}

	public static TaskStatus fromString(String value) {
		for (TaskStatus v : values())
			if (v.toString().equalsIgnoreCase(value))
				return v;
		throw new IllegalArgumentException();
	}

	@Override
	public String toString() {
		return status;
	}
}
