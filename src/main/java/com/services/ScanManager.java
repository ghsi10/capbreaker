package com.services;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.models.Chunk;
import com.models.Task;
import com.models.TaskStatus;
import com.repositories.TaskRepository;

@Service
public class ScanManager {
	private static final int CLIENT_TIMED_OUT = 30000;
	private static final int MAX_KEEP_ALIVE = 3;

	private final List<String[]> commands;

	private Task task;
	private List<String[]> scans;
	private List<Agent> agents;

	private TaskRepository taskRepository;

	public ScanManager(@Value("#{'${scan.commands}'.split(',')}") String[] commandsFromProperties) {
		commands = new ArrayList<String[]>();
		for (String command : commandsFromProperties)
			commands.add(command.split(" "));
		agents = new ArrayList<Agent>();
		scans = new ArrayList<String[]>();
		task = null;
	}

	public synchronized Chunk getNextTask(String username) throws EmptyResultDataAccessException {
		if (task == null)
			task = getWorkingTask();
		try {
			Agent agent = searchForAgent(username);
			return new Chunk(task.getHandshake(), agent.command);
		} catch (NameNotFoundException e) {
			return startNewScan(username);
		}
	}

	public synchronized void setResult(String username, String password) throws NameNotFoundException {
		Agent agent = searchForAgent(username);
		agent.interrupt();
		agents.remove(agent);
		reportThePassword(password);

	}

	public void keepAlive(String username) throws NameNotFoundException {
		Agent agent = searchForAgent(username);
		if (agent.keepAlive < MAX_KEEP_ALIVE)
			agent.keepAlive++;
	}

	private Chunk startNewScan(String username) throws EmptyResultDataAccessException {
		String[] command;
		Agent agent;
		synchronized (agents) {
			command = getNextCommand();
			agent = new Agent(username, command);
			agents.add(agent);

		}
		agent.start();
		return new Chunk(task.getHandshake(), command);
	}

	private String[] getNextCommand() throws EmptyResultDataAccessException {
		if (scans.isEmpty())
			throw new EmptyResultDataAccessException(1);
		return scans.remove(0);
	}

	private Agent searchForAgent(String username) throws NameNotFoundException {
		for (Agent agent : agents)
			if (agent.username.equals(username))
				return agent;
		throw new NameNotFoundException();
	}

	private void reportThePassword(String password) {
		if (!password.equals("") || isDone()) {
			synchronized (agents) {
				stopClients();
				scans.clear();
			}
			task.setStatus(TaskStatus.Completed);
			task.setWifiPassword(password);
			taskRepository.save(task);
			task = null;
		}
	}

	private boolean isDone() {
		synchronized (agents) {
			return scans.isEmpty() && agents.isEmpty();
		}
	}

	private void stopClients() {
		for (Agent agent : agents)
			agent.interrupt();
		agents.clear();
	}

	private Task getWorkingTask() throws EmptyResultDataAccessException {
		Task task = taskRepository.findOneByStatus(TaskStatus.Working);
		if (task == null) {
			List<Task> taskList = taskRepository.findAllByStatusOrderByIdAsc(TaskStatus.Queued);
			if (taskList == null || taskList.isEmpty())
				throw new EmptyResultDataAccessException(1);
			task = taskRepository.findAllByStatusOrderByIdAsc(TaskStatus.Queued).get(0);
			task.setStatus(TaskStatus.Working);
			taskRepository.save(task);
		}
		for (String[] command : commands)
			scans.add(command);
		return task;
	}

	private class Agent extends Thread {

		private final String username;
		private final String[] command;
		private int keepAlive;

		private Agent(String username, String[] command) {
			keepAlive = MAX_KEEP_ALIVE;
			this.command = command;
			this.username = username;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(CLIENT_TIMED_OUT);
					keepAlive--;
					if (keepAlive < 0) {
						scans.add(0, command);
						agents.remove(this);
						break;
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

	@Autowired
	public void setTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}
}
