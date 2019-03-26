package com.controllers;

import com.models.Chunk;
import com.services.ScanManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.naming.NameNotFoundException;
import java.rmi.NotBoundException;

@RestController
@RequestMapping("/agent")
public class AgentController {

    private ScanManager scanManager;

    @PostMapping("/getTask")
    public Chunk getTask() throws NotBoundException {
        return scanManager.getTask();
    }

    @PostMapping("/setResult")
    public void setResult(@RequestHeader String uuid, @RequestParam String password) throws NameNotFoundException {
        scanManager.setResult(uuid, password);
    }

    @PostMapping("/keepAlive")
    public void keepAlive(@RequestHeader String uuid) throws NameNotFoundException {
        scanManager.keepAlive(uuid);
    }

    @ExceptionHandler(NotBoundException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void handleNotBoundException() {
    }

    @ExceptionHandler(NameNotFoundException.class)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void handleNameNotFoundException() {
    }

    @Autowired
    public void setScanManager(ScanManager scanManager) {
        this.scanManager = scanManager;
    }
}
