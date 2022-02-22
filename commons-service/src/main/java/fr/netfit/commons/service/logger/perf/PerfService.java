package fr.netfit.commons.service.logger.perf;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.netfit.commons.service.error.ErrorRecord;
import fr.netfit.commons.service.request.RequestIdService;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;

@Slf4j
@Service
public class PerfService {

    private final Logger perfLogger = LoggerFactory.getLogger("PERF");

    private final RequestIdService requestIdService;
    private final ObjectMapper objectMapper;

    PerfService(RequestIdService requestIdService,
                ObjectMapper objectMapper) {
        this.requestIdService = requestIdService;
        this.objectMapper = objectMapper;
    }

    public Perf startPerf(String target, String action) {
        return new Perf(target, action, Instant.now());
    }

    public void ok(Perf perf) {
        end(perf, PerfEnum.OK, null);
    }

    public void error(Perf perf, Object error) {
        end(perf, PerfEnum.KO, error);
    }

    private void end(Perf perf, PerfEnum status, Object error) {
        PerfDto perfDto = new PerfDto(
                requestIdService.getRequestId(),
                perf.getTarget(),
                perf.getAction(),
                status,
                getDurationFrom(perf),
                computeErrorMessage(error),
                perf.getDetails());

        register(perfDto);
    }

    private String computeErrorMessage(Object error) {
        if (error == null) {
            return null;

        } else if (error instanceof String) {
            return (String) error;

        } else if (error instanceof Throwable) {
            return ((Throwable) error).getMessage();

        } else if (error instanceof ErrorRecord) {
            return toJson(error).orElseGet(((ErrorRecord) error)::getDetail);

        } else {
            return error.toString();
        }
    }

    private void register(PerfDto perfDto) {
        toJson(perfDto).ifPresent(perfLogger::info);
    }

    private Optional<String> toJson(Object object) {
        try {
            return Optional.of(objectMapper.writeValueAsString(object));
        } catch (JsonProcessingException e) {
            log.error("Une erreur est survenue lors de la serialisation d'un objet [{}]", object, e);
            return Optional.empty();
        }
    }

    private Duration getDurationFrom(Perf perf) {
        return Duration.between(perf.getStartTime(), Instant.now());
    }

}
