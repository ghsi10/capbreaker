package com.controllers;

import com.exceptions.NameNotFoundException;
import com.exceptions.NotBoundException;
import com.models.Chunk;
import com.services.AgentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private final AgentService agentService;

    @Autowired
    public AgentController(AgentService agentService) {
        this.agentService = agentService;
    }

    @PostMapping("/getTask")
    public Chunk getTask() throws NotBoundException {
        return agentService.getTask();
    }

    @PostMapping("/setResult")
    public void setResult(@RequestHeader String uuid, @RequestParam String password) throws NameNotFoundException {
        agentService.setResult(uuid, password);
    }

    @PostMapping("/keepAlive")
    public void keepAlive(@RequestHeader String uuid) throws NameNotFoundException {
        agentService.keepAlive(uuid);
    }

    @ExceptionHandler(NotBoundException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleNotBoundException() {
    }

    @ExceptionHandler(NameNotFoundException.class)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void handleNameNotFoundException() {
    }
}
