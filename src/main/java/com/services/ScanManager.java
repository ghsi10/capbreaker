package com.services;

import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

	private final List<String[]> commands;

	private LinkedList<ScanTask> tasks;
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
		AGENT_TIMER *= 1000;
		for (Task task : taskRepository.findAllByStatusOrderByIdAsc(TaskStatus.Working))
			addTaskToScanManager(task);
	}

	public Chunk getTask() throws NotBoundException {
		ScanTask scanTask;
		Entry<String, String[]> scanCommand;
		updateTasks();
		synchronized (tasks) {
			scanTask = tasks.peek();
			scanCommand = scanTask.pollCommand();
			if (scanTask.isEmpty())
				scanTask = tasks.poll();
			agents.add(new Agent(scanCommand.getKey(), scanTask.getTask(), scanCommand.getValue()));
		}
		return new Chunk(scanCommand.getKey(), scanTask.getTask().getHandshake(), scanCommand.getValue());
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
		// TODO write the metod
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
					Thread.sleep(AGENT_TIMER);
					keepAlive--;
					if (keepAlive < 0) {
						synchronized (tasks) {
							try {
								tasks.stream().filter(o -> o.getTask().equals(task)).findFirst().get().addCommand(uuid,
										command);
							} catch (NoSuchElementException e) {
								tasks.addFirst(new ScanTask(task, uuid, command));
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
