package com.services;

import com.exceptions.NameNotFoundException;
import com.exceptions.NotBoundException;
import com.models.Agent;
import com.models.Chunk;
import com.models.Scan;
import com.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
public class AgentService implements Runnable {

    @Value("${agent.idle.timer:60}")
    private int idleTimer;
    @Value("${agent.idle.max:60}")
    private int maxIdle;

    private final TaskRepository taskRepository;
    private final ScanManager scanManager;

    private final Map<Integer, AtomicInteger> taskStatus;
    private final Map<String, Agent> agents;
    private final Set<Integer> completedTasks;
    private final ReadWriteLock lock;

    @Autowired
    public AgentService(TaskRepository taskRepository, ScanManager scanManager) {
        this.taskRepository = taskRepository;
        this.scanManager = scanManager;
        completedTasks = Collections.synchronizedSet(new HashSet<>());
        taskStatus = new ConcurrentHashMap<>();
        agents = new ConcurrentHashMap<>();
        lock = new ReentrantReadWriteLock();
        Executors.newSingleThreadExecutor().submit(this);
    }

    public Chunk getTask() throws NotBoundException {
        lock.readLock().lock();
        try {
            Scan scan = scanManager.pop();
            while (completedTasks.contains(scan.getTask().getId())) {
                updateTaskStatus(scan.getTask().getId());
                scan = scanManager.pop();
            }
            String uuid = UUID.randomUUID().toString();
            int taskId = scan.getTask().getId();
            agents.put(uuid, new Agent(scan));
            if (!taskStatus.containsKey(taskId))
                taskStatus.put(taskId, new AtomicInteger(0));
            taskRepository.updateStatusToWorking(taskId);
            return new Chunk(uuid, scan.getTask().getHandshake(), scan.getCommands());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void setResult(String uuid, String password) throws NameNotFoundException {
        lock.readLock().lock();
        try {
            Agent agent = agents.remove(uuid);
            if (agent == null)
                throw new NameNotFoundException();
            int taskId = agent.getScan().getTask().getId();
            if (!completedTasks.contains(taskId)) {
                taskRepository.addProgress(taskId, scanManager.getProgress());
                if (!password.isEmpty()) {
                    completedTasks.add(taskId);
                    taskRepository.reportTheResult(taskId, password);
                }
            }
            updateTaskStatus(taskId);
        } finally {
            lock.readLock().unlock();
        }
    }

    public void keepAlive(String uuid) throws NameNotFoundException {
        lock.readLock().lock();
        try {
            Agent agent = agents.get(uuid);
            if (agent == null || completedTasks.contains(agent.getScan().getTask().getId()))
                throw new NameNotFoundException();
            agent.keepAlive();
        } finally {
            lock.readLock().unlock();
        }
    }

    void stopAgents(int taskId) {
        lock.writeLock().lock();
        try {
            scanManager.removeTask(taskId);
            agents.entrySet().removeIf(e -> e.getValue().getScan().getTask().getId().equals(taskId));
            taskStatus.entrySet().removeIf(e -> e.getKey().equals(taskId));
            completedTasks.removeIf(i -> i == taskId);
        } finally {
            lock.writeLock().unlock();
        }
    }

    void reset() {
        lock.writeLock().lock();
        try {
            scanManager.reset();
            agents.clear();
            taskStatus.clear();
            completedTasks.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public int agentCounter() {
        lock.readLock().lock();
        try {
            return agents.size();
        } finally {
            lock.readLock().unlock();
        }
    }

    private void updateTaskStatus(int taskId) {
        taskStatus.get(taskId).incrementAndGet();
        if (taskStatus.get(taskId).get() == scanManager.getCommandsSize()) {
            taskRepository.updateStatusToCompleted(taskId);
            completedTasks.remove(taskId);
            taskStatus.remove(taskId);
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(idleTimer);
                lock.readLock().lock();
                agents.entrySet().removeIf(a -> !taskStatus.containsKey(a.getValue().getScan().getTask().getId()));
                agents.entrySet().stream()
                        .filter(e -> LocalDateTime.now().minusSeconds(maxIdle).isAfter(e.getValue().getKeepAlive()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                        .forEach((k, v) -> {
                            if (agents.remove(k) != null)
                                scanManager.push(v.getScan());
                        });
            } catch (InterruptedException ignored) {
                break;
            } finally {
                lock.readLock().unlock();
            }
        }
    }
}
