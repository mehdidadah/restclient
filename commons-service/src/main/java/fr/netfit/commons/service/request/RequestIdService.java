package fr.netfit.commons.service.request;

import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Optional;
import java.util.UUID;

/**
 * Service permettant de récupérer ou dé générer le RequestId
 * @see fr.netfit.commons.service.request.RequestLogFilter
 */
@Service
public class RequestIdService {

    public static final String HEADER_REQUEST_ID = "X-REQUEST-ID";

    private static final String REQUEST_ID = "request.id";
    private static final String REQUEST_PATH = "request.path";

    /**
     * @return le requestId de la requête en cours
     */
    public String getRequestId() {
        return MDC.get(REQUEST_ID);
    }

    /**
     * Extrait le header requestId de la requête si il existe, créer un nouveau requestId sinon
     * Extrait également le path de la request
     * @param request La requête HTTTP reçu
     */
    void extractDataFromRequest(HttpServletRequest request) {
        String requestId = Optional
                .ofNullable(request.getHeader(HEADER_REQUEST_ID))
                .orElseGet(this::getNewRequestId);

        MDC.put(REQUEST_ID, requestId);
        MDC.put(REQUEST_PATH, request.getMethod() + ":" + request.getRequestURI());
    }

    void enhanceResponse(HttpServletResponse response) {
        response.setHeader(HEADER_REQUEST_ID, getRequestId());
    }

    void clean() {
        MDC.clear();
    }

    private String getNewRequestId() {
        UUID requestUUID = UUID.randomUUID();
        return requestUUID.toString();
    }
}
