package com.services;

import com.models.Task;
import com.repositories.TaskRepository;
import com.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Value("${spring.login.username}")
    private String MASTER_USERNAME;
    @Value("${spring.login.password}")
    private String MASTER_PASSWORD;

    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private ScanManager scanManager;

    public Task getResult(String taskId) throws NumberFormatException {
        Task task = taskRepository.findOne(Integer.parseInt(taskId));
        if (task != null)
            return task;
        throw new NumberFormatException();
    }

    public void deletTask(String taskId) {
        scanManager.stopTask(Integer.parseInt(taskId));
        taskRepository.delete(Integer.parseInt(taskId));
    }

    public String getPassword(String username) {
        if (username.equals(MASTER_USERNAME))
            return MASTER_PASSWORD;
        return userRepository.findOneByUsername(username).getPassword();
    }

    @Autowired
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Autowired
    public void setTaskRepository(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    @Autowired
    public void setScanManager(ScanManager scanManager) {
        this.scanManager = scanManager;
    }
}