package com.models;

import javax.persistence.*;
import java.security.SecureRandom;

// TODO: Add an binary array with the same length as the number of commands from the properties file.
// TODO: This array will be used to create a list of commands when adding a task to the scanTask list.
// TODO: This array will be updated when a user finished a task.
@Entity
@Table(name = "tasks")
public class Task {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;
	private String bssid;
	private String essid;
	private TaskStatus status;
	private String wifiPassword;
	private String taskPassword;
	@Lob
	private Handshake handshake;

	public Task() {
		this.status = TaskStatus.Queued;
		this.taskPassword = randomPassword();
		this.wifiPassword = "";
	}

	public Task(Task t) {
		this.id = t.id;
		this.bssid = t.bssid;
		this.essid = t.essid;
		this.status = t.status;
		this.taskPassword = t.taskPassword;
		this.handshake = t.handshake;
		this.taskPassword = randomPassword();
	}

	public Task(Handshake hs) {
		this.bssid = hs.getBssid();
		this.essid = hs.getEssid();
		this.status = TaskStatus.Queued;
		this.handshake = hs;
		this.wifiPassword = "";
		this.taskPassword = randomPassword();
	}

	private String randomPassword() {
		return String.valueOf(new SecureRandom().nextInt(89999) + 10000);
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getBssid() {
		return bssid;
	}

	public void setBssid(String bssid) {
		this.bssid = bssid;
	}

	public String getEssid() {
		return essid;
	}

	public void setEssid(String essid) {
		this.essid = essid;
	}

	public TaskStatus getStatus() {
		return status;
	}

	public void setStatus(TaskStatus status) {
		this.status = status;
	}

	public String getWifiPassword() {
		return wifiPassword;
	}

	public void setWifiPassword(String wifiPassword) {
		this.wifiPassword = wifiPassword;
	}

	public String getTaskPassword() {
		return taskPassword;
	}

	public void setTaskPassword(String taskPassword) {
		this.taskPassword = taskPassword;
	}

	public Handshake getHandshake() {
		return handshake;
	}

	public void setHandshake(Handshake handshake) {
		this.handshake = handshake;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Task) {
			Task task = (Task) obj;
			return task.id.equals(id) && task.essid.equals(essid) && task.bssid.equals(bssid);
		}
		return false;
	}

	@Override
	public String toString() {
		return "Task [id=" + id + ", bssid=" + bssid + ", essid=" + essid + ", status=" + status + ", wifiPassword="
				+ wifiPassword + ", taskPassword=" + taskPassword + ", handshake=" + handshake + "]";
	}
}
