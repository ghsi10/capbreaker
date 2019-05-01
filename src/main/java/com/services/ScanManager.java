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
    private final int progressEveryScan;

    private final TaskRepository taskRepository;

    @Autowired
    public ScanManager(@Value("#{'${scan.commands}'.split(',')}") String[] commandsFromProperties, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        commands = new ArrayList<>();
        Arrays.stream(commandsFromProperties).map(c -> c.split(" ")).forEach(commands::add);
        progressEveryScan = 100 / commandsFromProperties.length;
        tasks = new LinkedList<>();
        agents = new HashSet<>();
    }

    @PostConstruct
    private void init() {
        taskRepository.findAllByStatusOrderByIdAsc(TaskStatus.Working).forEach(task -> {
            task.setProgress(0);
            taskRepository.save(task);
            addTaskToScanManager(task);
        });
    }

    public Chunk getTask() throws NotBoundException {
        ScanTask scanTask;
        Pair<String, String[]> scanCommand;
        synchronized (tasks) {
            updateTasks();
            scanTask = tasks.get(0);
            scanCommand = scanTask.pollCommand();
            if (scanTask.isEmpty())
                tasks.remove(0);
            synchronized (agents) {
                Agent agent = new Agent(scanCommand.getFirst(), scanTask.getTask(), scanCommand.getSecond());
                agents.add(agent);
                agent.start();
            }
        }
        return new Chunk(scanCommand.getFirst(), scanTask.getTask().getHandshake(), scanCommand.getSecond());
    }

    public void setResult(String uuid, String password) throws NameNotFoundException {
        Agent agent;
        synchronized (agents) {
            agent = agents.stream().filter(a -> a.uuid.equals(uuid)).findFirst()
                    .orElseThrow(NameNotFoundException::new);
            agent.interrupt();
            agents.remove(agent);
        }
        taskRepository.addProgress(agent.task.getId(), progressEveryScan);
        reportTheResult(agent.task, password);

    }

    public void keepAlive(String uuid) throws NameNotFoundException {
        agents.stream().filter(a -> a.uuid.equals(uuid)).findFirst()
                .orElseThrow(NameNotFoundException::new).keepAlive = MAX_KEEP_ALIVE;
    }

    void stopTask(int taskId) {
        synchronized (tasks) {
            tasks.removeIf(s -> s.getTask().getId() == taskId);
            synchronized (agents) {
                agents.stream().filter(a -> a.task.getId() == taskId).collect(Collectors.toSet()).forEach(a -> {
                    a.interrupt();
                    agents.remove(a);
                });
            }
        }
    }

    private void reportTheResult(Task task, String password) {
        if (!password.equals("") || isTaskDone(task)) {
            taskRepository.reportTheResult(task.getId(), password);
            stopTask(task.getId());
        }
    }

    private boolean isTaskDone(Task task) {
        boolean isT, isA;
        synchronized (tasks) {
            isT = tasks.stream().noneMatch(s -> s.getTask().equals(task));
            synchronized (agents) {
                isA = agents.stream().noneMatch(a -> a.task.equals(task));
            }
        }
        return isT && isA;
    }

    private void updateTasks() throws NotBoundException {
        if (!tasks.isEmpty())
            return;
        Task task = taskRepository.getNextTask().orElseThrow(NotBoundException::new);
        addTaskToScanManager(task);
        taskRepository.updateStatusToWorking(task.getId());
    }

    private void addTaskToScanManager(Task task) {
        ScanTask scanTask = new ScanTask(task);
        commands.forEach(c -> scanTask.addCommand(UUID.randomUUID().toString(), c));
        tasks.add(scanTask);
    }

    public int agentCounter() {
        return agents.size();
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
                        Optional<ScanTask> optScanTask = tasks.stream()
                                .filter(s -> s.getTask().equals(task)).findFirst();
                        if (optScanTask.isPresent())
                            optScanTask.get().addCommand(uuid, command);
                        else
                            tasks.add(0, new ScanTask(task, uuid, command));
                        synchronized (agents) {
                            agents.remove(this);
                        }
                    }
                    break;
                }
            }
        }
    }
}
