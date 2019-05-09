package com.models;

public class Scan {

    private Task task;
    private String[] commands;

    public Scan(Task task, String[] commands) {
        this.task = task;
        this.commands = commands;
    }

    public Task getTask() {
        return task;
    }

    public void setTask(Task task) {
        this.task = task;
    }

    public String[] getCommands() {
        return commands;
    }

    public void setCommands(String[] commands) {
        this.commands = commands;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Scan) {
            Scan scan = (Scan) obj;
            return scan.task.equals(task) && scan.commands.equals(commands);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Scan[" + "task=" + task + ", commands=" + commands + "]";
    }
}
