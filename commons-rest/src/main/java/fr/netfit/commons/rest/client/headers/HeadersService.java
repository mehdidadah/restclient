package fr.netfit.commons.rest.client.headers;

import fr.netfit.commons.rest.client.RestClientParams;
import fr.netfit.commons.rest.client.request.Request;
import fr.netfit.commons.service.request.RequestHeadersService;
import fr.netfit.commons.service.request.RequestIdService;
import lombok.AllArgsConstructor;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.propagation.Propagator;
import org.springframework.stereotype.Service;

import static fr.netfit.commons.service.request.RequestIdService.HEADER_REQUEST_ID;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Service
@AllArgsConstructor
public class HeadersService {

    private static final String CONTENT_TYPE = "Content-Type";

    private final RequestIdService requestIdService;
    private final RequestHeadersService requestHeadersService;
    private final Propagator propagator;

    public Headers computeHeaders(RestClientParams params, Request request, String jsonBody, Span span) {
        Headers headers = new Headers(request);

        if (jsonBody != null) {
            headers.setHeader(CONTENT_TYPE, APPLICATION_JSON_VALUE);
        }

        if (params.isHeadersForwardingEnabled()) {
            headers.merge(requestHeadersService.getTechnicalHeadersMap());
        }

        String requestId = requestIdService.getRequestId();

        span.tag("request.id", requestId);
        propagator.inject(span.context(), headers, Headers::addHeader);
        headers.setHeader(HEADER_REQUEST_ID, requestId);

        return headers;
    }
}
