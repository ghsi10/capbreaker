package com.services;

import com.exceptions.NotBoundException;
import com.models.Scan;
import com.models.Task;
import com.repositories.CommandRepository;
import com.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
public class ScanManager implements Runnable {

    @Value("${scan.buffer.sleep:10}")
    private int sleepTime;

    private final TaskRepository taskRepository;
    private final CommandRepository commandRepository;
    private final BlockingQueue<Scan> scans;
    private final BlockingQueue<Scan> fallback;

    private List<String[]> commands;
    private Thread addScansThread;

    @Autowired
    public ScanManager(TaskRepository taskRepository, CommandRepository commandRepository, @Value("${scan.buffer.size:10}") int capacity) {
        this.taskRepository = taskRepository;
        this.commandRepository = commandRepository;
        scans = new ArrayBlockingQueue<>(capacity);
        fallback = new LinkedBlockingQueue<>();
        addScansThread = new Thread(this);
    }

    @PostConstruct
    public void init() {
        commands = commandRepository.findAllByOrderByPriorityAsc().stream()
                .map(c -> c.getCommand().split(" "))
                .collect(Collectors.toList());
        taskRepository.resetTasks();
        addScansThread.start();
    }

    Scan pop() throws NotBoundException {
        try {
            return fallback.remove();
        } catch (NoSuchElementException ignore1) {
            try {
                return scans.remove();
            } catch (NoSuchElementException ignore2) {
                throw new NotBoundException();
            }
        }
    }

    void push(Scan scan) {
        fallback.add(scan);
    }

    int getCommandsSize() {
        return commands.size();
    }

    BigDecimal getProgress() {
        return BigDecimal.valueOf(((int) (Math.round(100d / commands.size() * 100))) / 100d);
    }

    synchronized void removeTask(int taskId) {
        stop();
        scans.removeIf(s -> s.getTask().getId().equals(taskId));
        fallback.removeIf(s -> s.getTask().getId().equals(taskId));
        addScansThread = new Thread(this);
        addScansThread.start();
    }

    synchronized void reset() {
        stop();
        scans.clear();
        fallback.clear();
        addScansThread = new Thread(this);
        init();
    }

    private void stop() {
        addScansThread.interrupt();
        try {
            addScansThread.join();
        } catch (InterruptedException ignore) {
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                Optional<Task> optTask = taskRepository.getNextTask();
                if (!optTask.isPresent()) {
                    TimeUnit.SECONDS.sleep(sleepTime);
                    continue;
                }
                Task task = optTask.get();
                taskRepository.markAsPulled(task.getId());
                for (String[] command : commands)
                    scans.put(new Scan(task, command));
            } catch (InterruptedException ignored) {
                break;
            }
        }
    }
}
