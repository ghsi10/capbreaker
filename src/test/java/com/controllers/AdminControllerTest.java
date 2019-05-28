package com.controllers;

import com.models.ScanCommand;
import com.models.Task;
import com.models.User;
import com.services.AgentService;
import com.services.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest({AdminController.class, AdviceController.class})
@WithMockUser(roles = "ADMIN")
public class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    DataSource dataSource;
    @MockBean
    AgentService agentService;

    @Test
    public void getScansTest() throws Exception {
        List<ScanCommand> commands = Collections.singletonList(new ScanCommand());
        doReturn(commands).when(userService).getCommands();
        mockMvc.perform(get("/admin/scans"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "scans"))
                .andExpect(model().attribute("commands", commands))
                .andExpect(view().name("user/scans"));
    }

    @Test
    public void postScansTest() throws Exception {
        doNothing().when(userService).resetCommands();
        mockMvc.perform(post("/admin/scans"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/scans"));
    }

    @Test
    public void deleteScansTest() throws Exception {
        doNothing().when(userService).deleteCommand(anyInt());
        mockMvc.perform(get("/admin/command/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/scans"));
    }

    @Test
    public void getEditScansTest() throws Exception {
        ScanCommand commands = new ScanCommand();
        doReturn(commands).when(userService).getCommand(anyInt());
        mockMvc.perform(get("/admin/command/edit/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "scans"))
                .andExpect(model().attribute("command", commands))
                .andExpect(view().name("user/command"));
    }

    @Test
    public void postEditScansTest() throws Exception {
        doNothing().when(userService).saveCommand(any());
        mockMvc.perform(post("/admin/command/edit/{id}", 1)
                .param("priority", "1")
                .param("command", "command"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/scans"));
    }

    @Test
    public void postEditScansWithoutIdTest() throws Exception {
        doNothing().when(userService).saveCommand(any());
        mockMvc.perform(post("/admin/command/edit/{id}", -1)
                .param("priority", "1")
                .param("command", "command"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/scans"));
    }

    @Test
    public void getResultScansTest() throws Exception {
        Task task = new Task();
        doReturn(task).when(userService).taskResult(any());
        mockMvc.perform(get("/admin/task/result").param("taskId", "t"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "result"))
                .andExpect(model().attribute("task", task))
                .andExpect(view().name("resultof"));
    }

    @Test
    public void getDeleteTaskTest() throws Exception {
        doNothing().when(userService).deleteTask(anyInt());
        mockMvc.perform(get("/admin/task/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
    }

    @Test
    public void getUsrsTest() throws Exception {
        List<User> users = Collections.singletonList(new User());
        doReturn(users).when(userService).getUsers();
        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "users"))
                .andExpect(model().attribute("users", users))
                .andExpect(view().name("user/users"));
    }

    @Test
    public void getEnableUserTest() throws Exception {
        doNothing().when(userService).enabledUser(anyInt(), any(boolean.class));
        mockMvc.perform(get("/admin/user/enabled/{id}", 1).param("enabled", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    public void getDeleteUserTest() throws Exception {
        doNothing().when(userService).deleteUser(anyInt());
        mockMvc.perform(get("/admin/user/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    public void getPromoteUserTest() throws Exception {
        doNothing().when(userService).promoteUser(anyInt(), any(boolean.class));
        mockMvc.perform(get("/admin/user/promote/{id}", 1).param("promote", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/users"));
    }

    @Test
    public void postResultNumberFormatExceptionTest() throws Exception {
        doThrow(NumberFormatException.class).when(userService).taskResult(any());
        mockMvc.perform(get("/admin/task/result")
                .param("taskId", "1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "result"))
                .andExpect(model().attribute("error", "Invalid task id."))
                .andExpect(view().name("result"));
    }
}
