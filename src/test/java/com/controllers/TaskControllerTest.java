package com.controllers;

import com.exceptions.UnsupportedDataTypeException;
import com.models.Task;
import com.services.AgentService;
import com.services.TaskService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest({TaskController.class, AdviceController.class})
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private TaskService taskService;
    @MockBean
    DataSource dataSource;
    @MockBean
    AgentService agentService;

    @Test
    public void getMainTest() throws Exception {
        List<Task> tasks = Collections.emptyList();
        doReturn(tasks).when(taskService).getTable(anyInt());
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "tasks"))
                .andExpect(model().attribute("tasks", tasks))
                .andExpect(view().name("tasks"));
    }

    @Test
    public void getTasksTest() throws Exception {
        List<Task> tasks = Collections.emptyList();
        doReturn(tasks).when(taskService).getTable(anyInt());
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "tasks"))
                .andExpect(model().attribute("tasks", tasks))
                .andExpect(view().name("tasks"));
    }

    @Test
    public void getUploadFileTest() throws Exception {
        mockMvc.perform(get("/upload/file"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "upload"))
                .andExpect(view().name("upload-file"));
    }

    @Test
    public void getUploadTextTest() throws Exception {
        mockMvc.perform(get("/upload/text"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "upload"))
                .andExpect(view().name("upload-text"));
    }

    @Test
    public void postUploadFileTest() throws Exception {
        Task task = new Task();
        MockMultipartFile file = new MockMultipartFile("files", "x.cap"
                , "text/plain", "cap".getBytes());
        doReturn(task).when(taskService).uploadFile(any(), any(), any());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload/file")
                .file("capFile", file.getBytes())
                .param("essid", "essid1")
                .param("bssid", "bssid1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "upload"))
                .andExpect(model().attribute("task", task))
                .andExpect(view().name("uploaded"));
    }

    @Test
    public void postUploadTextTest() throws Exception {
        Task task = new Task();
        doReturn(task).when(taskService).uploadText(any());
        mockMvc.perform(post("/upload/text")
                .param("pmkid", "123123:123123:123123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "upload"))
                .andExpect(model().attribute("task", task))
                .andExpect(view().name("uploaded"));
    }

    @Test
    public void getResultTest() throws Exception {
        mockMvc.perform(get("/result"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "result"))
                .andExpect(view().name("result"));
    }

    @Test
    public void postResultTest() throws Exception {
        Task task = new Task();
        doReturn(task).when(taskService).getResult(any(), any());
        mockMvc.perform(post("/result")
                .param("taskId", "1")
                .param("taskPassword", "123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "result"))
                .andExpect(model().attribute("task", task))
                .andExpect(view().name("resultof"));
    }

    @Test
    public void postUploadFileUnsupportedDataTypeExceptionTest() throws Exception {
        UnsupportedDataTypeException exception = new UnsupportedDataTypeException("error1", "upload-file");
        MockMultipartFile file = new MockMultipartFile("files", "x.cap"
                , "text/plain", "cap".getBytes());
        doThrow(exception).when(taskService).uploadFile(any(), any(), any());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload/file")
                .file("capFile", file.getBytes())
                .param("essid", "essid1")
                .param("bssid", "bssid1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "upload"))
                .andExpect(model().attribute("error", "error1"))
                .andExpect(view().name("upload-file"));
    }

    @Test
    public void postUploadTextUnsupportedDataTypeExceptionTest() throws Exception {
        UnsupportedDataTypeException exception = new UnsupportedDataTypeException("error1", "upload-text");
        doThrow(exception).when(taskService).uploadText(any());
        mockMvc.perform(post("/upload/text")
                .param("pmkid", "123123:123123:123123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "upload"))
                .andExpect(model().attribute("error", "error1"))
                .andExpect(view().name("upload-text"));
    }

    @Test
    public void postUploadFileArrayIndexOutOfBoundsExceptionTest() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "x.cap"
                , "text/plain", "cap".getBytes());
        doThrow(ArrayIndexOutOfBoundsException.class).when(taskService).uploadFile(any(), any(), any());
        mockMvc.perform(MockMvcRequestBuilders.multipart("/upload/file")
                .file("capFile", file.getBytes())
                .param("essid", "essid1")
                .param("bssid", "bssid1"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "upload"))
                .andExpect(model().attribute("error", "Unexpected error"))
                .andExpect(view().name("upload-file"));
    }

    @Test
    public void postResultNumberFormatExceptionTest() throws Exception {
        doThrow(NumberFormatException.class).when(taskService).getResult(any(), any());
        mockMvc.perform(post("/result")
                .param("taskId", "1")
                .param("taskPassword", "123"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "result"))
                .andExpect(model().attribute("error", "Invalid task id and password."))
                .andExpect(view().name("result"));
    }
}
