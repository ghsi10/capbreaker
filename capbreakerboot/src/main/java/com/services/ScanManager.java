package com.services;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.models.ChunkData;
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
	private List<Client> clients;

	private TaskRepository taskRepository;

	public ScanManager(@Value("#{'${scan.commands}'.split(',')}") String[] commandsFromProperties) {
		commands = new ArrayList<String[]>();
		for (String command : commandsFromProperties)
			commands.add(command.split(" "));
		clients = new ArrayList<Client>();
		scans = new ArrayList<String[]>();
		task = null;
	}

	public synchronized ChunkData getNextTask(String username) throws EmptyResultDataAccessException {
		if (task == null)
			task = getWorkingTask();
		ChunkData chunkData = isAlreadyHaveJob(username);
		return chunkData != null ? chunkData : startNewScan(username);
	}

	public synchronized void setResult(String username, String password) throws NameNotFoundException {
		Client client = searchForClient(username);
		client.interrupt();
		clients.remove(client);
		reportThePassword(password);

	}

	public void keepAlive(String username) throws NameNotFoundException {
		Client client = searchForClient(username);
		if (client.keepAlive < MAX_KEEP_ALIVE)
			client.keepAlive++;
	}

	private ChunkData isAlreadyHaveJob(String username) {
		for (Client client : clients)
			if (client.username.equals(username))
				return new ChunkData(task.getHandshake(), client.command);
		return null;
	}

	private ChunkData startNewScan(String username) throws EmptyResultDataAccessException {
		String[] command = getNextCommand();
		synchronized (clients) {
			Client client = new Client(username, command);
			clients.add(client);
			client.start();
		}
		return new ChunkData(task.getHandshake(), command);
	}

	private String[] getNextCommand() throws EmptyResultDataAccessException {
		if (scans.isEmpty())
			throw new EmptyResultDataAccessException(1);
		return scans.remove(0);
	}

	private Client searchForClient(String username) throws NameNotFoundException {
		for (Client client : clients)
			if (client.username.equals(username))
				return client;
		throw new NameNotFoundException();
	}

	private void reportThePassword(String password) {
		if (!password.equals("") || isDone()) {
			scans.clear();
			stopClients();
			task.setStatus(TaskStatus.Completed);
			task.setWifiPassword(password);
			taskRepository.save(task);
			task = null;
		}
	}

	private boolean isDone() {
		synchronized (clients) {
			return scans.isEmpty() && clients.isEmpty();
		}
	}

	private void stopClients() {
		for (Client client : clients)
			client.interrupt();
		clients.clear();
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

	private class Client extends Thread {

		private final String username;
		private final String[] command;
		private int keepAlive;

		private Client(String username, String[] command) {
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
						clients.remove(this);
						scans.add(0, command);
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
