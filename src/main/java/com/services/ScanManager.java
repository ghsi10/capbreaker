package com.services;

import com.models.Chunk;
import com.models.ScanTask;
import com.models.Task;
import com.models.TaskStatus;
import com.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.naming.NameNotFoundException;
import java.rmi.NotBoundException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ScanManager {

    @Value("${agent.keepalive.timer}")
    private int AGENT_TIMER;
    @Value("${agent.keepalive.max}")
    private int MAX_KEEP_ALIVE;

    private final List<String[]> commands;
    private final List<ScanTask> tasks;
    private final Set<Agent> agents;

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

    public void setResult(String uuid, String password) throws NameNotFoundException {
        synchronized (tasks) {
            Optional<Agent> optAgent = agents.stream().filter(agent -> agent.uuid.equals(uuid)).findFirst();
            if (!optAgent.isPresent())
                throw new NameNotFoundException();
            Agent agent = optAgent.get();
            agent.interrupt();
            agents.remove(agent);
            reportTheResult(agent.task, password);
        }
    }

    public void keepAlive(String uuid) throws NameNotFoundException {
        Optional<Agent> optAgent = agents.stream().filter(agent -> agent.uuid.equals(uuid)).findFirst();
        if (optAgent.isPresent())
            optAgent.get().keepAlive = MAX_KEEP_ALIVE;
        else
            throw new NameNotFoundException();
    }

    public void stopTask(int taskId) {
        tasks.removeIf(scanTask -> scanTask.getTask().getId() == taskId);
        agents.stream().filter(agent -> agent.task.getId() == taskId).collect(Collectors.toList()).forEach(agent -> {
            agent.interrupt();
            agents.remove(agent);
        });
    }

    private void reportTheResult(Task task, String password) {
        if (!password.equals("") || tasks.stream().noneMatch(scanTask -> scanTask.getTask().equals(task))
                && agents.stream().noneMatch(agent -> agent.task.equals(task))) {
            taskRepository.reportTheResult(task.getId(), password);
            tasks.removeIf(scanTask -> scanTask.getTask().equals(task));
            agents.stream().filter(agent -> agent.task.equals(task)).collect(Collectors.toList()).forEach(agent -> {
                agent.interrupt();
                agents.remove(agent);
            });
        }
    }

    private synchronized void updateTasks() throws NotBoundException {
        if (!tasks.isEmpty())
            return;
        Task task = taskRepository.getNextTask();
        if (task == null)
            throw new NotBoundException();
        addTaskToScanManager(task);
        taskRepository.updateStatusToWorking(task.getId());
    }

    private void addTaskToScanManager(Task task) {
        ScanTask scanTask = new ScanTask(task);
        for (String[] command : commands)
            scanTask.addCommand(UUID.randomUUID().toString(), command);
        tasks.add(scanTask);
    }

    @Autowired
    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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
            while (true) {
                try {
                    TimeUnit.SECONDS.sleep(AGENT_TIMER);
                } catch (InterruptedException e) {
                    break;
                }
                keepAlive--;
                if (keepAlive < 0) {
                    synchronized (tasks) {
                        Optional<ScanTask> optScanTask = tasks.stream().filter(scanTask -> scanTask.getTask().equals
                                (task)).findFirst();
                        if (optScanTask.isPresent())
                            optScanTask.get().addCommand(uuid, command);
                        else
                            tasks.add(0, new ScanTask(task, uuid, command));
                        agents.remove(this);
                    }
                    break;
                }
            }

        }
    }
}
