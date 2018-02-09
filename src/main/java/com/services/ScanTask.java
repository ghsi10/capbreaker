package com.services;

import com.models.Task;

import java.util.LinkedList;

/**
 * A scan task contains a task from the task table and possible scan commands.<br>
 * The scan commands linked list is created initially from the commands in the properties file, but then gets updated
 * when agents are being assigned with scan tasks (- each agent performs a different scan command).<br>
 */
public class ScanTask {

    /** The task from the task table. */
    private Task task;
    /** The scanning commands - updated as agents receive their tasks */
    private LinkedList<String[]> scanCommands;

    ScanTask(Task task, LinkedList<String[]> scanCommands) {
        this.task = task;
        this.scanCommands = scanCommands;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public LinkedList<String[]> getScanCommands() {
        return scanCommands;
    }
}
