package fr.netfit.commons.service.request;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

/**
 * Classe premettant de loguer les requêtes reçues
 */
@Slf4j
@Component
public class RequestLogFilter extends OncePerRequestFilter {

    private static final Logger requestLogger = LoggerFactory.getLogger("REQUEST");

    private final RequestIdService requestIdService;
    private final RequestHeadersService requestHeadersService;

    public RequestLogFilter(RequestIdService requestIdService,
                            RequestHeadersService requestHeadersService) {
        this.requestIdService = requestIdService;
        this.requestHeadersService = requestHeadersService;
    }

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Instant startTime = Instant.now();

            requestIdService.extractDataFromRequest(request);
            requestIdService.enhanceResponse(response);

            log.info("reception de la requete [{} {} {}] depuis l'ip [{}]",
                    request.getProtocol(),
                    request.getMethod(),
                    request.getRequestURL(),
                    request.getRemoteAddr());

            requestHeadersService.extractHeaders(request);

            filterChain.doFilter(request, response);

            Duration requestDuration = Duration.between(startTime, Instant.now());

            log.info("requete traitee en [{}ms] avec le status [{}]", requestDuration.toMillis(), response.getStatus());
            requestLogger.info("{\"protocol\":\"{}\",\"method\":\"{}\",\"URL\":\"{}\",\"remoteAddr\":\"{}\",\"duration\":\"{}\",\"status\":\"{}\"}",
                    request.getProtocol(),
                    request.getMethod(),
                    request.getRequestURL(),
                    request.getRemoteAddr(),
                    requestDuration.toMillis(),
                    response.getStatus());

        } finally {
            requestHeadersService.clean();
            requestIdService.clean();
        }
    }

}
