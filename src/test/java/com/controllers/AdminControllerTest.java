package com.controllers;

import com.models.ScanCommand;
import com.models.Task;
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
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest({AdminController.class, AdviceController.class})
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
    @WithMockUser(roles = "ADMIN")
    public void getScansPass() throws Exception {
        List<ScanCommand> commands = Collections.singletonList(new ScanCommand());
        doReturn(commands).when(userService).getCommands();
        mockMvc.perform(get("/admin/scans"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "scans"))
                .andExpect(model().attribute("commands", commands))
                .andExpect(view().name("user/scans"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void postScansPass() throws Exception {
        doNothing().when(userService).resetCommands();
        mockMvc.perform(post("/admin/scans"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/scans"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void deleteScansPass() throws Exception {
        doNothing().when(userService).deleteCommand(anyInt());
        mockMvc.perform(get("/admin/command/delete/{id}", 1))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/scans"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getEditScansPass() throws Exception {
        ScanCommand commands = new ScanCommand();
        doReturn(commands).when(userService).getCommand(anyInt());
        mockMvc.perform(get("/admin/command/edit/{id}", 1))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "scans"))
                .andExpect(model().attribute("command", commands))
                .andExpect(view().name("user/command"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void postEditScansPass() throws Exception {
        doNothing().when(userService).saveCommand(any());
        mockMvc.perform(post("/admin/command/edit/{id}", 1)
                .param("priority", "1")
                .param("command", "command"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/scans"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void getResultScansPass() throws Exception {
        Task task = new Task();
        doReturn(task).when(userService).taskResult(any());
        mockMvc.perform(get("/admin/task/result").param("taskId", "t"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "result"))
                .andExpect(model().attribute("task", task))
                .andExpect(view().name("resultof"));
    }


}
