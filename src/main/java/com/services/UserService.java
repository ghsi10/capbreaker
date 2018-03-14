package com.services;

import com.models.Task;
import com.models.User;
import com.models.UserRole;
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
    
    public User signUp(String username, String password, String passwordAgain) {
        User user = null;
        
        if (password.equals(passwordAgain)) {
            // TODO: For now I created registered user as enabled, later should change it to User(username, password) ctor.
            user = new User(username, password, UserRole.ROLE_USER, true);
            
            // TODO: For now I catch everything and returns null. Later should change it to support different kinds of messages.
            try {
                userRepository.save(user);
            } catch (Exception exp) {
                return null;
            }
        }
        
        return user;
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
