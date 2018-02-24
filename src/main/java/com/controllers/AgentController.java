package com.controllers;

import java.rmi.NotBoundException;
import java.util.NoSuchElementException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.models.Chunk;
import com.services.ScanManager;

@RestController
public class AgentController {

	private ScanManager scanManager;

	@RequestMapping(value = "/agent/getTask", method = RequestMethod.POST)
	public ResponseEntity<Chunk> getTask() throws NotBoundException {
		return new ResponseEntity<>(scanManager.getTask(), HttpStatus.OK);
	}

	@RequestMapping(value = "/agent/setResult", method = RequestMethod.POST)
	public void setResult(@RequestHeader String uuid, @RequestParam String password) throws NoSuchElementException {
		scanManager.setResult(uuid, password);
	}

	@RequestMapping(value = "/agent/keepAlive", method = RequestMethod.POST)
	public void keepAlive(@RequestHeader String uuid) throws NoSuchElementException {
		scanManager.keepAlive(uuid);
	}

	@ExceptionHandler(NotBoundException.class)
	public ResponseEntity<?> handleDbError(NotBoundException e) {
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ExceptionHandler(NoSuchElementException.class)
	public ResponseEntity<?> handleDbError(NoSuchElementException e) {
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Autowired
	public void setScanManager(ScanManager scanManager) {
		this.scanManager = scanManager;
	}
}
