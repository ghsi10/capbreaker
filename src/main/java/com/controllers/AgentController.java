package com.controllers;

import com.models.Chunk;
import com.services.ScanManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.naming.NameNotFoundException;
import java.rmi.NotBoundException;

@RestController
public class AgentController {

    private ScanManager scanManager;

    @RequestMapping(value = "/agent/getTask", method = RequestMethod.POST)
    public ResponseEntity<Chunk> getTask() throws NotBoundException {
        return new ResponseEntity<>(scanManager.getTask(), HttpStatus.OK);
    }

    @RequestMapping(value = "/agent/setResult", method = RequestMethod.POST)
    public void setResult(@RequestHeader String uuid, @RequestParam String password) throws NameNotFoundException {
        scanManager.setResult(uuid, password);
    }

    @RequestMapping(value = "/agent/keepAlive", method = RequestMethod.POST)
    public void keepAlive(@RequestHeader String uuid) throws NameNotFoundException {
        scanManager.keepAlive(uuid);
    }

    @ExceptionHandler(NotBoundException.class)
    public ResponseEntity<?> handleNotBoundException() {
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @ExceptionHandler(NameNotFoundException.class)
    public ResponseEntity<?> handleNameNotFoundException() {
        return new ResponseEntity<>(HttpStatus.ACCEPTED);
    }

    @Autowired
    public void setScanManager(ScanManager scanManager) {
        this.scanManager = scanManager;
    }
}
