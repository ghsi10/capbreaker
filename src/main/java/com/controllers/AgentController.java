package com.controllers;

import java.rmi.NotBoundException;

import javax.naming.NameNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.models.Chunk;
import com.services.ScanManager;

@RestController
public class AgentController {

	private ScanManager scanManager;

	@RequestMapping(value = "/agent/login", method = RequestMethod.POST)
	public ResponseEntity<?> getNextTask() {
		return new ResponseEntity<>(HttpStatus.OK);
	}

	@RequestMapping(value = "/agent/getNextTask", method = RequestMethod.POST)
	public ResponseEntity<Chunk> getNextTask(@AuthenticationPrincipal User user) throws NotBoundException {
		return new ResponseEntity<>(scanManager.getNextTask(user.getUsername()), HttpStatus.OK);
	}

	@RequestMapping(value = "/agent/setResult", method = RequestMethod.POST)
	public void setResult(@AuthenticationPrincipal User user, @RequestParam String password)
			throws NameNotFoundException {
		scanManager.setResult(user.getUsername(), password);
	}

	@RequestMapping(value = "/agent/keepAlive", method = RequestMethod.POST)
	public void keepAlive(@AuthenticationPrincipal User user) throws NameNotFoundException {
		scanManager.keepAlive(user.getUsername());
	}

	@ExceptionHandler(NotBoundException.class)
	public ResponseEntity<?> handleDbError(NotBoundException e) {
		return new ResponseEntity<>(HttpStatus.NO_CONTENT);
	}

	@ExceptionHandler(NameNotFoundException.class)
	public ResponseEntity<?> handleDbError(NameNotFoundException e) {
		return new ResponseEntity<>(HttpStatus.ACCEPTED);
	}

	@Autowired
	public void setScanManager(ScanManager scanManager) {
		this.scanManager = scanManager;
	}
}
