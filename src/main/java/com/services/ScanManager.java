package com.services;

import com.models.Chunk;
import com.models.ScanTask;
import com.models.Task;
import com.models.TaskStatus;
import com.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.naming.NameNotFoundException;
import java.rmi.NotBoundException;
import java.util.*;

/**
 * The scan manager manages gives scan tasks to scanning agents.
 */
@Service public class ScanManager {
	private static final int AGENT_TIMED_OUT = 30000;
	private static final int MAX_KEEP_ALIVE = 3;

	/** The raw commands that will be used to scan during a task. */
	private final List<String[]> commands;
	/** The list of tasks that will be given to scanning agents. */
	private final LinkedList<ScanTask> tasks;
	/** The scanning agents. An agent can scan a command for a given scan task. */
	private final Set<ScanningAgent> agents;
	/** The task repository from which scan tasks are created. */
	private TaskRepository taskRepository;

	public ScanManager(@Value("#{'${scan.commands}'.split(',')}") String[] commandsFromProperties) {
		commands = new ArrayList<>();
		for (String command : commandsFromProperties)
			commands.add(command.split(" "));
		tasks = new LinkedList<>(); // not thread safe
		agents = new LinkedHashSet<>(); // not thread safe
	}

	/**
	 * An client side application calls this method to get a scanning task.<br>
	 * A client is registered as a scanning agent and the given a scanning task.<br>
	 *
	 * @param username
	 *         the username of the agent
	 * @return a Chunk for the agent to scan
	 * @throws NotBoundException
	 *         if taskRepository wasn't reachable
	 */
	public Chunk getNextTask(String username) throws NotBoundException {
		ScanTask scanTask;
		String[] scanCommand;
		synchronized (tasks) {
			if (tasks.isEmpty()) {
				updateTasks();
			}

			// get a scan task and a scan command for it
			scanTask = tasks.peek();
			scanCommand = scanTask.getScanCommands().poll();
			if (null == scanTask.getScanCommands().peek()) {
				// scanningTask is out of scan commands, remove it from queue:
				scanTask = tasks.poll();
			}
		}

		try {
			ScanningAgent agent = searchForAgent(username);
			// agent found, send it a new Chunk with an updated scan command
			agent.scanTask = scanTask;
			agent.activeScanCommand = scanCommand;
			return new Chunk(scanTask.getTask().getHandshake(), scanCommand);
		} catch (NameNotFoundException e) {
			// agent not found, create a new one and send it a new Chunk
			initNewScanningAgent(username, scanTask, scanCommand);
			return new Chunk(scanTask.getTask().getHandshake(), scanCommand);
		}
	}

	/**
	 * An agent reports scanning results via this method.<br>
	 * The reporting agent is removed from the agents list.<br>
	 *
	 * @param username
	 *         the username of the agent
	 * @param password
	 *         the password of the agent
	 * @throws NameNotFoundException
	 *         if the agent wasn't found in the agents list
	 */
	public synchronized void setResult(String username, String password) throws NameNotFoundException {
		ScanningAgent agent = searchForAgent(username);
		ScanTask finishedScanTask = agent.scanTask;
		agent.interrupt();
		synchronized (agents) {
			agents.remove(agent);
		}
		reportThePassword(password, finishedScanTask);
	}

	/**
	 * An agent reports his still alive using this method.<br>
	 * If an agent stop reporting, its keep alive value will reach to 0 and the
	 * agent will be deleted.<br>
	 *
	 * @param username
	 *         the username of the agent
	 * @throws NameNotFoundException
	 *         if the agent wasn't found in the agents list
	 */
	public void keepAlive(String username) throws NameNotFoundException {
		ScanningAgent agent = searchForAgent(username);
		if (agent.keepAlive < MAX_KEEP_ALIVE) {
			agent.keepAlive++;
		}
	}

	@Autowired public void setTaskRepository(TaskRepository taskRepository) {
		this.taskRepository = taskRepository;
	}

	/**
	 * Create a new scanning agent with a scan task and a scan command to perform,
	 * and add it to the agents list.<br>
	 *
	 * @param username
	 *         the username of the agent
	 * @param scanTask
	 *         the scan task that this agent will scan for
	 * @param scanCommand
	 *         the current scan command the agent will perform
	 */
	private void initNewScanningAgent(String username, ScanTask scanTask, String[] scanCommand) {
		ScanningAgent agent;
		agent = new ScanningAgent(username, scanTask, scanCommand);
		synchronized (agents) {
			agents.add(agent);
		}
		agent.start();
	}

	/**
	 * Find an agent in the agents list.<br>
	 *
	 * @param username
	 *         the username of the agent
	 * @return the scanning agent from the agents list
	 * @throws NameNotFoundException
	 *         if the agent wasn't found in the agents list
	 */
	private ScanningAgent searchForAgent(String username) throws NameNotFoundException {
		synchronized (agents) {
			for (ScanningAgent agent : agents) {
				if (agent.username.equals(username)) {
					return agent;
				}
			}
		}
		throw new NameNotFoundException();
	}

	/**
	 * Update the task status in the task table (index page) if password was found
	 * or all scan commands were used.<br>
	 *
	 * @param password
	 *         the password that the agent discovered
	 * @param finishedScanTask
	 *         the task that the agent was working on
	 */
	private void reportThePassword(String password, ScanTask finishedScanTask) {
		// password found or task is not in the task list anymore:
		if (!password.equals("") || !tasks.contains(finishedScanTask)) {
			// updating task status:
			finishedScanTask.getTask().setStatus(TaskStatus.Completed);
			finishedScanTask.getTask().setWifiPassword(password);
			taskRepository.save(finishedScanTask.getTask());
			// remove scan task from the task list:
			synchronized (tasks) {
				tasks.remove(finishedScanTask);
			}
			// remove all agents that are still working on that scan task:
			synchronized (agents) {
				for (ScanningAgent agent : agents) {
					if (agent.scanTask.equals(finishedScanTask)) {
						agent.interrupt();
						agents.remove(agent);
					}
				}
			}
		} // TODO: else - update the commands array in the Task instance in the database (turn off the relevant bit)
	}

	/**
	 * Update the local task list by accessing the task table from the database. <br>
	 *
	 * @throws NotBoundException
	 *         if taskRepository wasn't reachable
	 */
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
			tasks.add(new ScanTask(task, new LinkedList<>(commands)));
		} else { // add all tasks with "Working" state
			for (Task task : taskList) {
				// TODO: Use the commands array from the Task model when creating the second parameter:
				tasks.add(new ScanTask(task, new LinkedList<>(commands)));
			}
		}
	}

	/**
	 * A keep alive thread for a scanning agent.<br>
	 * If an agents keep alive reached to 0 - the thread will re-add the agents scan
	 * command to the relevant scan task in the task list.
	 */
	private class ScanningAgent extends Thread {

		/** The agents username. */
		private final String username;
		/** The scan task the agents was assigned. */
		private ScanTask scanTask;
		/** The current scan command the agent is running. */
		private String[] activeScanCommand;
		/** The agents keep alive. 0 means dead. */
		private int keepAlive;

		private ScanningAgent(String username, ScanTask scanTask, String[] activeScanCommand) {
			this.username = username;
			this.scanTask = scanTask;
			this.activeScanCommand = activeScanCommand;
			this.keepAlive = MAX_KEEP_ALIVE;
		}

		@Override public void run() {
			try {
				while (true) {
					Thread.sleep(AGENT_TIMED_OUT);
					keepAlive--;
					if (keepAlive < 0) {
						// re-add the scan command of this agent to the scan task:
						synchronized (tasks) {
							// update the scan task in the scan task list
							if (tasks.contains(scanTask)) {
								// TODO: Update the commands array in the Task model (turn on the relevant bit)
								tasks.get(tasks.indexOf(scanTask)).getScanCommands().add(activeScanCommand);
							} else { // or - add the scan task again at the top of the queue (top priority):
								// No need to update the commands array in the Task model
								scanTask.getScanCommands().clear();
								scanTask.getScanCommands().add(activeScanCommand);
								tasks.addFirst(scanTask);
							}
						}
						synchronized (agents) {
							agents.remove(this);
						}
						break;
					}
				}
			} catch (InterruptedException e) {
				// an agent had finished its current task
			}
		}
	}
}
