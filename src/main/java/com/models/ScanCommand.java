package com.models;

import javax.persistence.*;

@Entity
@Table(name = "commands")
public class ScanCommand {

    public static final int NO_ID = -1;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private Integer priority;
    private String command;

    public ScanCommand() {
        id = NO_ID;
    }

    public ScanCommand(Integer id, Integer priority, String command) {
        this.id = id;
        this.priority = priority;
        this.command = command;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ScanCommand) {
            ScanCommand scanCommand = (ScanCommand) obj;
            return scanCommand.priority.equals(priority) && scanCommand.command.equals(command);
        }
        return false;
    }

    @Override
    public String toString() {
        return "ScanCommand [id=" + id + ", priority=" + priority + ", command='" + command + "]";
    }
}
