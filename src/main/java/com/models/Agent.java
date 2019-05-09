package com.models;

import java.time.LocalDateTime;

public class Agent {

    private final Scan scan;
    private LocalDateTime keepAlive;

    public Agent(Scan scan) {
        this.scan = scan;
        keepAlive = LocalDateTime.now();
    }

    public Scan getScan() {
        return scan;
    }

    public LocalDateTime getKeepAlive() {
        return keepAlive;
    }

    public void keepAlive() {
        this.keepAlive = LocalDateTime.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Agent) {
            Agent agent = (Agent) obj;
            return agent.scan.equals(scan);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Agent[scan=" + scan + ", keepAlive=" + keepAlive + "]";
    }
}
