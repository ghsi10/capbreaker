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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
public class ScanManager {

    private final TaskRepository taskRepository;
    private final List<String[]> commands;
    private final Object lock;
    private List<Scan> scans;


    @Autowired
    public ScanManager(@Value("#{'${scan.commands}'.split(',')}") String[] commands, TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
        this.commands = new ArrayList<>();
        scans = new ArrayList<>();
        lock = new Object();
        Arrays.stream(commands).map(c -> c.split(" ")).forEach(this.commands::add);
    }

    @PostConstruct
    private void init() {
        taskRepository.findAllByStatusOrderByIdAsc(TaskStatus.Working).forEach(task -> {
            task.setProgress(0);
            taskRepository.save(task);
            commands.forEach(c -> scans.add(new Scan(task, c)));
        });
    }

    public Scan pop() throws NotBoundException {
        synchronized (lock) {
            if (scans.isEmpty()) {
                Task task = taskRepository.getNextTask().orElseThrow(NotBoundException::new);
                commands.forEach(c -> scans.add(new Scan(task, c)));
                taskRepository.updateStatusToWorking(task.getId());
            }
            return scans.remove(0);
        }
    }

    public void push(Scan scan) {
        synchronized (lock) {
            scans.add(0, scan);
        }
    }

    public boolean isDone(Task task) {
        synchronized (lock) {
            return scans.stream().noneMatch(s -> s.getTask().equals(task));
        }
    }

    public void remove(Task task) {
        synchronized (lock) {
            scans.removeIf(s -> s.getTask().equals(task));
        }
    }
}
