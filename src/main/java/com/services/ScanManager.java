package com.services;

import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import com.models.Chunk;
import com.models.ScanTask;
import com.models.Task;
import com.models.TaskStatus;
import com.repositories.TaskRepository;

@Service
public class ScanManager {

	@Value("${agent.keepalive.timer}")
	private int AGENT_TIMER;
	@Value("${agent.keepalive.max}")
	private int MAX_KEEP_ALIVE;

	private List<String[]> commands;
	private List<ScanTask> tasks;
	private Set<Agent> agents;

	private TaskRepository taskRepository;

	public ScanManager(@Value("#{'${scan.commands}'.split(',')}") String[] commandsFromProperties) {
		commands = new ArrayList<>();
		for (String command : commandsFromProperties)
			commands.add(command.split(" "));
		tasks = new LinkedList<>();
		agents = new HashSet<>();
	}

	@PostConstruct
	private void init() {
		for (Task task : taskRepository.findAllByStatusOrderByIdAsc(TaskStatus.Working))
			addTaskToScanManager(task);
	}

	public Chunk getTask() throws NotBoundException {
		ScanTask scanTask;
		Pair<String, String[]> scanCommand;
		updateTasks();
		synchronized (tasks) {
			scanTask = tasks.get(0);
			scanCommand = scanTask.pollCommand();
			if (scanTask.isEmpty())
				scanTask = tasks.remove(0);
			Agent agent = new Agent(scanCommand.getFirst(), scanTask.getTask(), scanCommand.getSecond());
			agents.add(agent);
			agent.start();
		}
		return new Chunk(scanCommand.getFirst(), scanTask.getTask().getHandshake(), scanCommand.getSecond());
	}

	public void setResult(String uuid, String password) throws NoSuchElementException {
		synchronized (tasks) {
			Agent agent = agents.stream().filter(o -> o.uuid.equals(uuid)).findFirst().get();
			agent.interrupt();
			agents.remove(agent);
			reportTheResult(agent.task, password);
		}
	}

	public void keepAlive(String uuid) throws NoSuchElementException {
		agents.stream().filter(o -> o.uuid.equals(uuid)).findFirst().get().keepAlive = MAX_KEEP_ALIVE;
	}

	public void stopTask(int taskId) {
		tasks.stream().filter(o -> o.getTask().getId() == taskId).collect(Collectors.toList()).forEach(tasks::remove);
		agents.stream().filter(o -> o.task.getId() == taskId).collect(Collectors.toList()).forEach(o -> {
			o.interrupt();
			agents.remove(o);
		});
	}

	private void reportTheResult(Task task, String password) {
		if (!password.equals("") || !tasks.stream().anyMatch(o -> o.getTask().equals(task))
				&& !agents.stream().anyMatch(o -> o.task.equals(task))) {
			task.setStatus(TaskStatus.Completed);
			task.setWifiPassword(password);
			taskRepository.save(task);
			tasks.stream().filter(o -> o.getTask().equals(task)).collect(Collectors.toList()).forEach(tasks::remove);
			agents.stream().filter(o -> o.task.equals(task)).collect(Collectors.toList()).forEach(o -> {
				o.interrupt();
				agents.remove(o);
			});
		}
	}

	private synchronized void updateTasks() throws NotBoundException {
		if (!tasks.isEmpty())
			return;
		List<Task> taskList = taskRepository.findAllByStatusOrderByIdAsc(TaskStatus.Queued);
		if (taskList.isEmpty())
			throw new NotBoundException();
		Task task = taskList.get(0);
		task.setStatus(TaskStatus.Working);
		addTaskToScanManager(task);
		taskRepository.save(task);
	}

	private void addTaskToScanManager(Task task) {
		ScanTask scanTask = new ScanTask(task);
		for (String[] command : commands)
			scanTask.addCommand(UUID.randomUUID().toString(), command);
		tasks.add(scanTask);
	}

	private class Agent extends Thread {

		private final String uuid;
		private final Task task;
		private final String[] command;
		private int keepAlive;

		private Agent(String uuid, Task task, String[] command) {

			this.uuid = uuid;
			this.task = task;
			this.command = command;
			keepAlive = MAX_KEEP_ALIVE;
		}

		@Override
		public void run() {
			try {
				while (true) {
					TimeUnit.SECONDS.sleep(AGENT_TIMER);
					keepAlive--;
					if (keepAlive < 0) {
						synchronized (tasks) {
							try {
								tasks.stream().filter(o -> o.getTask().equals(task)).findFirst().get().addCommand(uuid,
										command);
							} catch (NoSuchElementException e) {
								tasks.add(0, new ScanTask(task, uuid, command));
							}
							agents.remove(this);
						}
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
