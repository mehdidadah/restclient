package fr.netfit.commons.service.logger.perf;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonInclude(NON_EMPTY)
public record PerfDto(@JsonProperty("request.id") String requestId,
                      @JsonProperty("target") String target,
                      @JsonProperty("action") String action,
                      @JsonProperty("status") PerfEnum status,
                      @JsonProperty("duration") long duration,
                      @JsonProperty("error") String error,
                      @JsonProperty("details") Map<String, Object> details) {

}
