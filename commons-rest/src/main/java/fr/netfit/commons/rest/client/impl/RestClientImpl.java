package fr.netfit.commons.rest.client.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.netfit.commons.rest.client.RequestSender;
import fr.netfit.commons.rest.client.RestClient;
import fr.netfit.commons.rest.client.RestClientParameters;
import fr.netfit.commons.rest.client.headers.Headers;
import fr.netfit.commons.rest.client.headers.HeadersService;
import fr.netfit.commons.rest.client.health.StatusProvider;
import fr.netfit.commons.rest.client.request.ErrorHandler;
import fr.netfit.commons.rest.client.request.GetRequest;
import fr.netfit.commons.rest.client.request.PostRequest;
import fr.netfit.commons.rest.client.request.Request;
import fr.netfit.commons.rest.client.response.Response;
import fr.netfit.commons.service.error.ServiceException;
import fr.netfit.commons.service.logger.perf.Perf;
import fr.netfit.commons.service.logger.perf.PerfService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.sleuth.Span;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.cloud.sleuth.Tracer.SpanInScope;
import org.springframework.lang.Nullable;

import java.net.http.HttpTimeoutException;
import java.util.Optional;
import java.util.concurrent.TimeoutException;

import static fr.netfit.commons.rest.client.error.ErrorHandlerImpl.asStringErrorHandler;
import static fr.netfit.commons.service.error.ErrorEnum.APPLICATION_ERROR;
import static fr.netfit.commons.service.error.ErrorEnum.APPLICATION_TIMEOUT;
import static javax.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static javax.servlet.http.HttpServletResponse.SC_NO_CONTENT;
import static org.apache.commons.lang3.StringUtils.isBlank;

@Slf4j
public class RestClientImpl implements RestClient, StatusProvider {

    private final RestClientParameters parameters;
    private final HeadersService headersService;
    private final Tracer tracer;

    private final ObjectMapper objectMapper;
    private final PerfService perfService;

    private final RequestSender requestSender;

    private final ErrorHandler<String> defaultErrorHandler;

    public RestClientImpl(RestClientParameters parameters,
                          HeadersService headersService,
                          Tracer tracer,
                          ObjectMapper objectMapper,
                          PerfService perfService, RequestSender requestSender) {
        this.parameters = parameters;
        this.headersService = headersService;
        this.tracer = tracer;
        this.objectMapper = objectMapper;
        this.perfService = perfService;
        this.requestSender = requestSender;
        this.defaultErrorHandler = asStringErrorHandler(objectMapper, parameters.getDefaultErrorHandler());
    }

    @Override
    public <T> Optional<T> get(GetRequest<T> request) {
        Response<String> response = execute(request, null);
        return deserializeResponseBody(response, request.getResponseType());
    }

    @Override
    public <T> Optional<T> post(PostRequest<T> request) {
        Response<String> response = execute(request, bodyToJson(request.getBody()));
        return deserializeResponseBody(response, request.getResponseType());
    }

    @Override
    public Response<String> getStatus() {
        GetRequest<String> statusRequest = GetRequest.<String>builder()
                .url("/ping")
                .responseType(String.class)
                .build();

        return execute(statusRequest, null);
    }

    private String bodyToJson(Object body) {
        try {
            return objectMapper.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            log.error("Une erreur est survenue durant la sérialisation de la requête", e);
            throw new ServiceException(APPLICATION_ERROR);
        }
    }

    private Response<String> execute(Request request, @Nullable String body) {
        String target = parameters.getRootUri().toASCIIString() + request.getUrl();
        String action = request.getMethod();

        Perf perf = perfService.startPerf(target, action);

        Span span = tracer.nextSpan().name(action + "::" + target);
        try (SpanInScope ignored = tracer.withSpan(span.start())) {

            Response<String> response;
            if (request.getUrl().contains("ping")) {
                response = requestSender.ping();
            } else {
                Headers headers = headersService.computeHeaders(parameters, request, body, span);
                response = requestSender.sendRequest(request, headers, body);
            }

            perf.addDetails("http.status", response.getStatus());
            span.tag("http.status", String.valueOf(response.getStatus()));

            if (response.getStatus() < SC_BAD_REQUEST) {
                perfService.ok(perf);
                return response;

            } else {
                String responseBody = response.getBody();
                perfService.error(perf, responseBody);
                span.tag("error", responseBody);

                ErrorHandler<String> errorHandler = getErrorHandler(request.getErrorHandler());
                throw errorHandler.handleError(response);
            }

        } catch (HttpTimeoutException tex) {
            perfService.error(perf, tex);
            span.error(tex);
            throw new ServiceException(APPLICATION_TIMEOUT);

        } catch (ServiceException sex) {
            throw sex;

        } catch (Exception ex) {
            log.error("Une erreur est survenue lors de l'exécution de la requête http", ex);
            perfService.error(perf, ex);
            span.error(ex);
            throw new ServiceException(APPLICATION_ERROR);
        } finally {
            span.end();
        }
    }

    private <T> Optional<T> deserializeResponseBody(Response<String> response, Class<T> responseType) {
        if (responseType == Void.class
                || response.getStatus() == SC_NO_CONTENT
                || isBlank(response.getBody())) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(objectMapper.readValue(response.getBody(), responseType));
        } catch (JsonProcessingException e) {
            log.error("Une erreur est survenue durant la désérialisation de la réponse", e);
            throw new ServiceException(APPLICATION_ERROR);
        }
    }

    public ErrorHandler<String> getErrorHandler(ErrorHandler<?> requestErrorHandler) {
        if (requestErrorHandler != null) {
            return asStringErrorHandler(objectMapper, requestErrorHandler);
        }
        return defaultErrorHandler;
    }
}
