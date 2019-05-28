package com.controllers;

import com.exceptions.NameNotFoundException;
import com.exceptions.NotBoundException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.models.Chunk;
import com.models.Handshake;
import com.services.AgentService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest({AgentController.class, AdviceController.class})
@WithMockUser(roles = "USER")
public class AgentControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private AgentService agentService;
    @MockBean
    DataSource dataSource;

    @Test
    public void getTaskTest() throws Exception {
        Chunk chunk = new Chunk("uuid1", new Handshake("hs1"), new String[]{"c1", "c2", "c3"});
        doReturn(chunk).when(agentService).getTask();
        mockMvc.perform(post("/agent/getTask"))
                .andExpect(status().isOk())
                .andExpect(content().json(new ObjectMapper().writeValueAsString(chunk)));
    }

    @Test
    public void getTaskNotFoundTest() throws Exception {
        doThrow(NotBoundException.class).when(agentService).getTask();
        mockMvc.perform(post("/agent/getTask")).andExpect(status().is2xxSuccessful());
    }

    @Test
    public void setResultTest() throws Exception {
        doNothing().when(agentService).setResult(any(), any());
        mockMvc.perform(post("/agent/setResult")
                .header("uuid", "uuid1")
                .param("password", "pass"))
                .andExpect(status().isOk());
    }

    @Test
    public void setResultAgentNotFoundTest() throws Exception {
        doThrow(NameNotFoundException.class).when(agentService).setResult(any(), any());
        mockMvc.perform(post("/agent/setResult")
                .header("uuid", "uuid1")
                .param("password", "pass"))
                .andExpect(status().is2xxSuccessful());
    }

    @Test
    public void keepAliveTest() throws Exception {
        doNothing().when(agentService).keepAlive(any());
        mockMvc.perform(post("/agent/keepAlive")
                .header("uuid", "uuid1"))
                .andExpect(status().isOk());
    }

    @Test
    public void keepAliveAgentNotFoundTest() throws Exception {
        doThrow(NameNotFoundException.class).when(agentService).keepAlive(any());
        mockMvc.perform(post("/agent/keepAlive")
                .header("uuid", "uuid1"))
                .andExpect(status().is2xxSuccessful());
    }
}
