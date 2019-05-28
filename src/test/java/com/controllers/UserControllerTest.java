package com.controllers;

import com.services.AgentService;
import com.services.UserService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@WebMvcTest({UserController.class, AdviceController.class})
@WithMockUser(username = "user")
@TestPropertySource(properties = {"agent.server.dns=1.1.1.1", "agent.download.url=0.0.0.0"})
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private UserService userService;
    @MockBean
    DataSource dataSource;
    @MockBean
    AgentService agentService;

    @Test
    public void getSigninTest() throws Exception {
        mockMvc.perform(get("/signin"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "signin"))
                .andExpect(view().name("user/signin"));
    }

    @Test
    public void getSigninLogoutTest() throws Exception {
        mockMvc.perform(get("/signin")
                .param("logout", "y"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "signin"))
                .andExpect(model().attribute("msg", "You have been logged out."))
                .andExpect(view().name("user/signin"));
    }

    @Test
    public void getSigninErrorTest() throws Exception {
        mockMvc.perform(get("/signin")
                .param("error", "e"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "signin"))
                .andExpect(model().attribute("error", "Invalid username and password."))
                .andExpect(view().name("user/signin"));
    }

    @Test
    public void getSigninErrorPendingTest() throws Exception {
        mockMvc.perform(get("/signin")
                .param("error", "e")
                .sessionAttr("SPRING_SECURITY_LAST_EXCEPTION", new DisabledException("")))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "signin"))
                .andExpect(model().attribute("error", "This user is pending to admin approval."))
                .andExpect(view().name("user/signin"));
    }

    @Test
    public void getSignupTest() throws Exception {
        mockMvc.perform(get("/signup"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("module", "signup"))
                .andExpect(view().name("user/signup"));
    }

    @Test
    public void postSignupUsernameLengthShortTest() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", "")
                .param("password", "12345678")
                .param("passwordAgain", "12345678"))
                .andExpect(model().attribute("module", "signup"))
                .andExpect(model().attribute("error", "Username/Password should be between 4 to 16"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"));
    }

    @Test
    public void postSignupUsernameLengthLongTest() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", "aaaaaaaaaaaaaaaaaaaa")
                .param("password", "12345678")
                .param("passwordAgain", "12345678"))
                .andExpect(model().attribute("module", "signup"))
                .andExpect(model().attribute("error", "Username/Password should be between 4 to 16"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"));
    }

    @Test
    public void postSignupPasswordLengthShortTest() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", "admin")
                .param("password", "")
                .param("passwordAgain", ""))
                .andExpect(model().attribute("module", "signup"))
                .andExpect(model().attribute("error", "Username/Password should be between 4 to 16"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"));
    }

    @Test
    public void postSignupPasswordLengthLongTest() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", "admin")
                .param("password", "aaaaaaaaaaaaaaaaaaaa")
                .param("passwordAgain", "aaaaaaaaaaaaaaaaaaaa"))
                .andExpect(model().attribute("module", "signup"))
                .andExpect(model().attribute("error", "Username/Password should be between 4 to 16"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"));
    }

    @Test
    public void postSignupUsernameBadFormatTest() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", "admin*")
                .param("password", "12345678")
                .param("passwordAgain", "12345678"))
                .andExpect(model().attribute("module", "signup"))
                .andExpect(model().attribute("error", "Username/Password contains illegal characters"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"));
    }

    @Test
    public void postSignupPasswordBadFormatTest() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", "admin")
                .param("password", "12345678*")
                .param("passwordAgain", "12345678*"))
                .andExpect(model().attribute("module", "signup"))
                .andExpect(model().attribute("error", "Username/Password contains illegal characters"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"));
    }

    @Test
    public void postSignupPasswordNotTheSameTest() throws Exception {
        mockMvc.perform(post("/signup")
                .param("username", "admin")
                .param("password", "12345678")
                .param("passwordAgain", "123456789"))
                .andExpect(model().attribute("module", "signup"))
                .andExpect(model().attribute("error", "Password does not match the confirm password"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/signup"));
    }

    @Test
    public void postSignupTest() throws Exception {
        doNothing().when(userService).signup(any(), any());
        mockMvc.perform(post("/signup")
                .param("username", "admin")
                .param("password", "12345678")
                .param("passwordAgain", "12345678"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));
    }

    @Test
    public void downloadAgentTest() throws Exception {
        doReturn("pass").when(userService).getPassword(any());
        mockMvc.perform(get("/user/download"))
                .andExpect(header().stringValues("Content-Disposition", "attachment; filename=\"CapBreakerAgent.py\""))
                .andExpect(model().attribute("username", "user"))
                .andExpect(model().attribute("password", "pass"))
                .andExpect(model().attribute("server", "1.1.1.1"))
                .andExpect(model().attribute("url", "0.0.0.0"))
                .andExpect(status().isOk())
                .andExpect(view().name("user/agent"));
    }
}
