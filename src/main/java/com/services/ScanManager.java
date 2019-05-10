package com.services;

import com.exceptions.NotBoundException;
import com.models.Scan;
import com.models.Task;
import com.models.TaskStatus;
import com.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

@Component
public class ScanManager implements Runnable {

    @Value("${scan.buffer.sleep}")
    private int sleepTime;

    private final TaskRepository taskRepository;
    private final List<String[]> commands;
    private final BlockingQueue<Scan> scans;
    private final BlockingQueue<Scan> fallback;
    private final Thread addScansThread;

    @Autowired
    public ScanManager(@Value("${scan.buffer.size}") int capacity,
                       @Value("#{'${scan.commands}'.split(',')}") String[] commands, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.commands = new ArrayList<>();
        scans = new ArrayBlockingQueue<>(capacity);
        fallback = new LinkedBlockingQueue<>();
        Arrays.stream(commands).map(c -> c.split(" ")).forEach(this.commands::add);
        addScansThread = new Thread(this);
    }

    @PostConstruct
    public void init() {
        taskRepository.resetTasks();
        addScansThread.start();
    }

    Scan pop() throws NotBoundException {
        Scan scans = getNext();
        while (!check(scans))
            scans = getNext();
        return scans;
    }

    void push(Scan scan) {
        fallback.add(scan);
    }

    int getCommandsSize() {
        return commands.size();
    }

    private boolean check(Scan scan) {
        Optional<Task> optTask = taskRepository.findById(scan.getTask().getId());
        return optTask.isPresent() && !optTask.get().getStatus().equals(TaskStatus.Completed);
    }

    private Scan getNext() throws NotBoundException {
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
