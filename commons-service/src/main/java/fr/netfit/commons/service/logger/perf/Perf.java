package fr.netfit.commons.service.logger.perf;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public record Perf(String target,
                   String action,
                   Instant startTime,
                   Map<String, Object> details) {

    Perf(String target, String action, Instant startTime) {
        this(target, action, startTime, new HashMap<>());
    }

    public void addDetails(String name, Object value) {
        details.put(name, value);
    }

}
