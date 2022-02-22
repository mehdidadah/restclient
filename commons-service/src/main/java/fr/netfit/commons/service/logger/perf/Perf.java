package fr.netfit.commons.service.logger.perf;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class Perf {

    private final String target;
    private final String action;
    private final Instant startTime;
    private final Map<String, Object> details;

    Perf(String target, String action, Instant startTime) {
        this.target = target;
        this.action = action;
        this.startTime = startTime;
        this.details = new HashMap<>();
    }

    String getTarget() {
        return target;
    }

    String getAction() {
        return action;
    }

    Instant getStartTime() {
        return startTime;
    }

    Map<String, Object> getDetails() {
        return details;
    }

    public void addDetails(String name, Object value) {
        details.put(name, value);
    }

}
