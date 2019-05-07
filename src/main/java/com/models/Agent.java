package com.models;

import java.time.LocalDate;
import java.util.UUID;

public class Agent {

    private final String uuid;
    private final Scan scan;
    private LocalDate keepAlive;

    public Agent(Scan scan) {
        this.scan = scan;
        uuid = UUID.randomUUID().toString();
        keepAlive = LocalDate.now();
    }

    public String getUuid() {
        return uuid;
    }

    public Scan getScan() {
        return scan;
    }

    public LocalDate getKeepAlive() {
        return keepAlive;
    }

    public void keepAlive() {
        this.keepAlive = LocalDate.now();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Agent) {
            Agent agent = (Agent) obj;
            return agent.uuid.equals(uuid) && agent.scan.equals(scan);
        }
        return false;
    }

    @Override
    public String toString() {
        return "Agent[" + "uuid='" + uuid + '\'' + ", scan=" + scan + ", keepAlive=" + keepAlive + "]";
    }
}
