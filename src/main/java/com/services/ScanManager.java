package com.services;

import java.rmi.NotBoundException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;

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

	@Value("${agent.keepalvie.timer}")
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

	public Chunk getTask() throws NotBoundException {
		ScanTask scanTask;
		Entry<String, String[]> scanCommand;
		synchronized (tasks) {
			if (tasks.isEmpty())
				updateTasks();
			scanTask = tasks.peek();
			scanCommand = scanTask.pollCommand();
			if (scanTask.isEmpty())
				scanTask = tasks.poll();
			agents.add(new Agent(scanCommand.getKey(), scanTask.getTask(), scanCommand.getValue()));
		}
		return new Chunk(scanCommand.getKey(), scanTask.getTask().getHandshake(), scanCommand.getValue());
	}

	public synchronized void setResult(String uuid, String password) throws NoSuchElementException {
		Agent agent = agents.stream().filter(o -> o.uuid.equals(uuid)).findFirst().get();
		synchronized (tasks) {
			agent.interrupt();
			agents.remove(agent);
			reportThePassword(agent.task, password);
		}
	}

	public void keepAlive(String uuid) throws NoSuchElementException {
		Agent agent = agents.stream().filter(o -> o.uuid.equals(uuid)).findFirst().get();
		if (agent.keepAlive < MAX_KEEP_ALIVE) {
			agent.keepAlive++;
		}
	}

	private void reportThePassword(Task task, String password) {
		if (!password.equals("") || !tasks.stream().anyMatch(o -> o.getTask().equals(task))) {
			task.setStatus(TaskStatus.Completed);
			task.setWifiPassword(password);
			taskRepository.save(task);
			tasks.stream().filter(o -> o.getTask().equals(task)).forEach(tasks::remove);
			agents.stream().filter(o -> o.task.equals(task)).forEach(o -> o.interrupt());
			agents.stream().filter(o -> o.task.equals(task)).forEach(agents::remove);
		}
	}

	private void updateTasks() throws NotBoundException {
		List<Task> taskList = taskRepository.findAllByStatusOrderByIdAsc(TaskStatus.Working);
		if (taskList == null || taskList.isEmpty()) { // no previous working tasks, get a new one
			taskList = taskRepository.findAllByStatusOrderByIdAsc(TaskStatus.Queued);
			if (taskList == null || taskList.isEmpty()) {
				throw new NotBoundException();
			}
			Task task = taskList.get(0);
			task.setStatus(TaskStatus.Working);
			taskRepository.save(task);
			tasks.add(new ScanTask());
		} else { // add all tasks with "Working" state
			for (Task task : taskList) {
				// TODO: Use the commands array from the Task model when creating the second
				// parameter:
				tasks.add(new ScanTask());
			}
		}
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
			this.keepAlive = MAX_KEEP_ALIVE;
		}

		@Override
		public void run() {
			try {
				while (true) {
					Thread.sleep(AGENT_TIMER);
					keepAlive--;
					if (keepAlive < 0) {
						synchronized (tasks) {
							if (tasks.stream().anyMatch(o -> o.getTask().equals(task)))
								tasks.stream().filter(o -> o.getTask().equals(task)).findFirst().get().addCommand(uuid,
										command);
							else
								tasks.addFirst(new ScanTask(task, uuid, command));
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
