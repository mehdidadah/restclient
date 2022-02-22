package fr.netfit.commons.service.logger.perf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

import java.time.Duration;
import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
@Getter
public class PerfDto {

    @JsonProperty("request.id")
    private final String requestId;

    @JsonProperty("target")
    private final String target;

    @JsonProperty("action")
    private final String action;

    @JsonProperty("status")
    private final PerfEnum status;

    @JsonProperty("duration")
    private final long duration;

    @JsonProperty("error")
    private final String error;

    @JsonProperty("details")
    private final Map<String, Object> details;

    PerfDto(String requestId, String target, String action, PerfEnum status, Duration duration, String error, Map<String, Object> details) {
        this.requestId = requestId;
        this.target = target;
        this.action = action;
        this.status = status;
        this.duration = duration.toMillis();
        this.error = error;
        this.details = details;
    }

}
