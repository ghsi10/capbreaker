package com.services;

import com.models.Handshake;
import com.models.Task;
import com.repositories.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import javax.activation.UnsupportedDataTypeException;
import java.util.List;
import java.util.Optional;

@Service
public class TaskService {

    @Value("${view.page.size}")
    private int pageSize;

    private final TaskRepository taskRepository;
    private final HashConvert hashConvert;

    @Autowired
    public TaskService(TaskRepository taskRepository, HashConvert hashConvert) {
        this.taskRepository = taskRepository;
        this.hashConvert = hashConvert;
    }

    public List<Task> getTable(int page) {
        return taskRepository.findAllByOrderByIdDesc(PageRequest.of(page, pageSize)).getContent();
    }

    public Task uploadCap(byte[] file, String essid, String bssid) throws UnsupportedDataTypeException {
        Handshake handshake = hashConvert.convert(file, essid, bssid);
        Optional<Task> optTask = taskRepository.findByHandshake(handshake);
        if (optTask.isPresent())
            throw new UnsupportedDataTypeException("This handshake already exists, task id:" + optTask.get().getId());
        Task task = new Task(handshake);
        taskRepository.save(task);
        return task;
    }

    public Task getResult(String taskId, String taskPassword) throws NumberFormatException {
        return taskRepository.findById(Integer.parseInt(taskId))
                .filter(t -> t.getTaskPassword().equals(taskPassword)).orElseThrow(NumberFormatException::new);
    }
}
