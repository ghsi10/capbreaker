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

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class AgentService implements Runnable {

    @Value("${agent.idle.timer}")
    private int idleTimer;
    @Value("${agent.idle.max}")
    private int maxIdle;

    private final TaskRepository taskRepository;
    private final Thread keepAliveThread;
    private final ScanManager scanManager;
    private final Map<Integer, Integer> taskStatus;
    private final Map<String, Agent> agents;

    @Autowired
    public AgentService(TaskRepository taskRepository, ScanManager scanManager) {
        this.taskRepository = taskRepository;
        this.scanManager = scanManager;
        taskStatus = new HashMap<>();
        agents = new HashMap<>();
        keepAliveThread = new Thread(this);
    }

    @PostConstruct
    public void init() {
        keepAliveThread.start();
    }

    public Chunk getTask() throws NotBoundException {
        Scan scan = scanManager.pop();
        String uuid = UUID.randomUUID().toString();
        int taskId = scan.getTask().getId();
        agents.put(uuid, new Agent(scan));
        taskStatus.put(taskId, 0);
        taskRepository.updateStatusToWorking(taskId);
        return new Chunk(uuid, scan.getTask().getHandshake(), scan.getCommands());
    }

    public synchronized void setResult(String uuid, String password) throws NameNotFoundException {
        Agent agent = agents.remove(uuid);
        if (agent == null)
            throw new NameNotFoundException();
        int taskId = agent.getScan().getTask().getId();
        if (!taskStatus.containsKey(taskId))
            throw new NameNotFoundException();
        int maxSize = scanManager.getCommandsSize();
        if (password.isEmpty()) {
            taskStatus.put(taskId, taskStatus.get(taskId) + 1);
            taskRepository.addProgress(taskId, 100 / maxSize);
        }
        if (!password.isEmpty() || taskStatus.get(taskId) == maxSize) {
            taskStatus.remove(taskId);
            taskRepository.reportTheResult(taskId, password);
        }
    }

    public void keepAlive(String uuid) throws NameNotFoundException {
        Agent agent = agents.get(uuid);
        if (agent == null || !taskStatus.containsKey(agent.getScan().getTask().getId()))
            throw new NameNotFoundException();
        agent.keepAlive();
    }

    void stopAgents(int taskId) {
        taskStatus.remove(taskId);
    }

    public int agentCounter() {
        return agents.size();
    }

    @Override
    public void run() {
        while (true) {
            try {
                TimeUnit.SECONDS.sleep(idleTimer);
                agents.entrySet().stream()
                        .filter(e -> LocalDateTime.now().minusSeconds(maxIdle).isAfter(e.getValue().getKeepAlive()))
                        .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue))
                        .forEach((k, v) -> {
                            scanManager.push(v.getScan());
                            agents.remove(k);
                        });
            } catch (InterruptedException ignored) {
                break;
            }
        }
    }
}
