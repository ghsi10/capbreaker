package com.services;

import java.util.ArrayList;
import java.util.List;

import javax.naming.NameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

import com.dao.TaskDao;
import com.models.ChunkData;
import com.models.Task;
import com.models.TaskStatus;

@Service
public class ScanManager {
	private static final int CLIENT_TIMED_OUT = 30000;
	private static final int MAX_KEEP_ALIVE = 3;

	private static final int COMPLETED = 2;
	private static final int SCANING = 1;
	private static final int QUEUE = 0;

	private final List<String[]> commands;

	private Task task;
	private int[] scans;
	private List<Client> clients;

	@Autowired
	private TaskDao taskDao;

	@Autowired
	public ScanManager(@Value("#{'${scan.commands}'.split(',')}") String[] commandsFromProperties) {
		commands = new ArrayList<String[]>();
		for (String command : commandsFromProperties)
			commands.add(command.split(" "));
		clients = new ArrayList<Client>();
		scans = new int[commands.size()];
		task = null;
	}

	public synchronized ChunkData getNextTask(String username) throws EmptyResultDataAccessException {
		ChunkData userChunkData = isAlreadyHaveJob(username);
		if (userChunkData != null)
			return userChunkData;
		if (task == null)
			task = getWorkingTask();
		return startNewScan(username, findTaskNumber());
	}

	public synchronized void setResult(String username, String password) throws NameNotFoundException {
		Client tmpClient = searchForClient(username);
		tmpClient.interrupt();
		clients.remove(tmpClient);
		scans[tmpClient.index] = COMPLETED;
		reportThePassword(password);

	}

	public void keepAlive(String username) throws NameNotFoundException {
		for (Client client : clients)
			if (client.username.equals(username)) {
				if (client.keepAlive < MAX_KEEP_ALIVE)
					client.keepAlive++;
				return;
			}
		throw new NameNotFoundException();
	}

	private ChunkData isAlreadyHaveJob(String username) {
		for (Client client : clients)
			if (client.username.equals(username))
				return new ChunkData(task.getHandshake(), commands.get(client.index));
		return null;
	}

	private int findTaskNumber() throws EmptyResultDataAccessException {
		for (int i = 0; i < scans.length; i++)
			if (scans[i] == QUEUE)
				return i;
		throw new EmptyResultDataAccessException(1);
	}

	private ChunkData startNewScan(String username, int scanNumber) {
		scans[scanNumber] = SCANING;
		Client tmpClient = new Client(username, scanNumber);
		clients.add(tmpClient);
		tmpClient.start();
		return new ChunkData(task.getHandshake(), commands.get(scanNumber));
	}

	private Client searchForClient(String username) throws NameNotFoundException {
		for (Client client : clients)
			if (client.username.equals(username))
				return client;
		throw new NameNotFoundException();
	}

	private void reportThePassword(String password) {
		if (!password.equals("") || isDone()) {
			Task tmpTask = new Task(task);
			clearScanMannager();
			tmpTask.setStatus(TaskStatus.Completed);
			tmpTask.setWifiPassword(password);
			taskDao.save(tmpTask);

		}
	}

	private boolean isDone() {
		for (int scan : scans)
			if (scan != COMPLETED)
				return false;
		return true;
	}

	private void clearScanMannager() {
		for (Client client : clients)
			client.interrupt();
		clients.clear();
		task = null;
		for (int i = 0; i < scans.length; i++)
			scans[i] = QUEUE;
	}

	private class Client extends Thread {

		private final String username;
		private final int index;
		private int keepAlive;

		private Client(String username, int index) {
			keepAlive = MAX_KEEP_ALIVE;
			this.index = index;
			this.username = username;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Client.sleep(CLIENT_TIMED_OUT);
					keepAlive--;
					if (keepAlive < 0) {
						clients.remove(this);
						scans[index] = QUEUE;
						break;
					}
				}
			} catch (InterruptedException e) {
			}
		}
	}

	private Task getWorkingTask() throws EmptyResultDataAccessException {
		Task task = taskDao.findOneByStatus(TaskStatus.Working);
		if (task != null)
			return task;
		List<Task> taskList = taskDao.findAllByStatusOrderByIdAsc(TaskStatus.Queued);
		if (taskList == null || taskList.isEmpty())
			throw new EmptyResultDataAccessException(1);
		task = taskDao.findAllByStatusOrderByIdAsc(TaskStatus.Queued).get(0);
		task.setStatus(TaskStatus.Working);
		taskDao.save(task);
		return task;
	}
}
