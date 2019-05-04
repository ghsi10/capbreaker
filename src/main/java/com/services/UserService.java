package com.services;

import com.exceptions.ValidationException;
import com.models.Task;
import com.models.User;
import com.models.UserRole;
import com.repositories.TaskRepository;
import com.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    @Value("${spring.login.username}")
    private String masterUsername;
    @Value("${spring.login.password}")
    private String masterPassword;
    @Value("${user.default.enable}")
    private boolean defaultEnable;

    private final UserRepository userRepository;
    private final TaskRepository taskRepository;
    private final ScanManager scanManager;

    @Autowired
    public UserService(UserRepository userRepository, TaskRepository taskRepository, ScanManager scanManager) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
        this.scanManager = scanManager;
    }

    public void signup(String username, String password) throws ValidationException {
        if (masterUsername.equals(username) || userRepository.findByUsername(username).isPresent())
            throw new ValidationException("Username is not available");
        userRepository.save(new User(username, password, UserRole.ROLE_USER, defaultEnable));
    }

    public Task taskResult(String taskId) throws NumberFormatException {
        return taskRepository.findById(Integer.parseInt(taskId)).orElseThrow(NumberFormatException::new);
    }

    public void deleteTask(int taskId) {
        scanManager.stopTask(taskId);
        taskRepository.deleteById(taskId);
    }

    public void deleteUser(int userId) {
        userRepository.deleteById(userId);
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
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        if (username.equals(masterUsername))
            return encoder.encode(masterPassword);
        return encoder.encode(userRepository.findByUsername(username).orElse(new User()).getPassword());
    }
}
