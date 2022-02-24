package fr.netfit.commons.service.request;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Locale.ROOT;
import static org.springframework.http.HttpHeaders.ACCEPT_LANGUAGE;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Slf4j
@Service
public class RequestHeadersService {

    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String X_FORWARDED_PORT = "X-Forwarded-Port";

    private static final Set<String> ALLOWED_TECHNICAL_HEADER_NAME_SET = Stream.of(X_FORWARDED_FOR, X_FORWARDED_PORT, ACCEPT_LANGUAGE)
            .map(String::toUpperCase)
            .collect(Collectors.toSet());

    private final ThreadLocal<Map<String, List<String>>> technicalHeadersHolder = new ThreadLocal<>();

    void extractHeaders(HttpServletRequest request) {
        List<String> headerNames = Collections.list(request.getHeaderNames());

        Map<String, List<String>> headers = new HashMap<>();
        for (String h : headerNames) {
            String headerName = h.toUpperCase(ROOT);
            List<String> headerValues = Collections.list(request.getHeaders(headerName));

            if (AUTHORIZATION.equalsIgnoreCase(headerName)) {
                log.info("Réception du HEADER[{}]", h);
            } else {
                headerValues.forEach(v -> log.info("Réception du HEADER[{}] = [{}]", h, v));
            }

            if (ALLOWED_TECHNICAL_HEADER_NAME_SET.contains(headerName)) {
                headers.put(headerName, unmodifiableList(headerValues));
            }
        }

        technicalHeadersHolder.set(unmodifiableMap(headers));
    }

    void clean() {
        technicalHeadersHolder.remove();
    }

    public Map<String, List<String>> getTechnicalHeadersMap() {
        return technicalHeadersHolder.get();
    }

}
