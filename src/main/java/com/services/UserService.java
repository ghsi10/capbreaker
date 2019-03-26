package com.services;

import com.models.Task;
import com.models.User;
import com.models.UserRole;
import com.repositories.TaskRepository;
import com.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Value("${view.page.size}")
    private int PAGE_SIZE;

    @Value("${spring.login.username}")
    private String MASTER_USERNAME;
    @Value("${spring.login.password}")
    private String MASTER_PASSWORD;

    @Value("${user.default.enable}")
    private boolean DEFAULT_ENABLE;

    private UserRepository userRepository;
    private TaskRepository taskRepository;
    private ScanManager scanManager;

    public void signup(String username, String password) throws NoSuchFieldException {
        if (MASTER_USERNAME.equals(username) || userRepository.findOneByUsername(username) != null)
            throw new NoSuchFieldException("Username is not available");
        User user = new User(username, password, UserRole.ROLE_USER, DEFAULT_ENABLE);
        userRepository.save(user);
    }

    public Task taskResult(String taskId) throws NumberFormatException {
        Task task = taskRepository.findOne(Integer.parseInt(taskId));
        if (task != null)
            return task;
        throw new NumberFormatException();
    }

    public void deleteTask(int taskId) {
        scanManager.stopTask(taskId);
        taskRepository.delete(taskId);
    }

    public void deleteUser(int userId) {
        userRepository.delete(userId);
    }

    public List<User> getUsers() {
        return userRepository.findAllByOrderByIdDesc();
    }

    public void enabledUser(int userId, boolean enabled) {
        userRepository.enabled(userId, enabled);
    }

    public void promoteUser(int userId, boolean promote) {
        UserRole userRole = UserRole.ROLE_USER;
        if (promote)
            userRole = UserRole.ROLE_ADMIN;
        userRepository.promote(userId, userRole);
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
