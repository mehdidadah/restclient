package fr.netfit.commons.rest.client.impl.rest;

import fr.netfit.commons.rest.client.RestClientParameters;
import fr.netfit.commons.rest.client.headers.Headers;
import fr.netfit.commons.rest.client.request.Request;
import fr.netfit.commons.rest.client.response.ResponseImpl;
import fr.netfit.commons.rest.client.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.web.util.DefaultUriBuilderFactory;
import org.springframework.web.util.UriBuilderFactory;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpRequest;
import java.net.http.HttpRequest.BodyPublisher;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.HttpRequest.Builder;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;

@Slf4j
public class RequestSenderMapper {

    private final RestClientParameters parameters;
    private final UriBuilderFactory factory;

    public RequestSenderMapper(RestClientParameters parameters) {
        this.parameters = parameters;
        this.factory = new DefaultUriBuilderFactory(parameters.getRootUri().toString());
    }

    HttpRequest mapToHttpRequest(Request request, Headers headers, @Nullable String body) {
        Builder builder = HttpRequest.newBuilder()
                .uri(createURI(request))
                .timeout(getTimeout(request))
                .method(request.getMethod(), prepareBody(body));

        headers.forEach(builder::header);

        return builder.build();
    }

    HttpURLConnection mapToHttpURLConnection() throws IOException {
        return (HttpURLConnection) new URL(parameters.getRootUri().toString())
                .openConnection();
    }

    Response<String> mapToResponse(HttpResponse<String> response) {
        Map<String, String> headers = new HashMap<>(response.headers().map().size());
        response.headers().map().forEach((k, v) -> headers.put(k, String.join(", ", v)));

        return ResponseImpl.<String>builder()
                .status(response.statusCode())
                .headers(headers)
                .body(response.body())
                .build();
    }


    private BodyPublisher prepareBody(@Nullable String body) {
        if (body != null) {
            return BodyPublishers.ofString(body, UTF_8);
        }

        return BodyPublishers.noBody();
    }

    private URI createURI(Request request) {
        return factory.expand(request.getUrl(), request.getParameters());
    }

    private Duration getTimeout(Request request) {
        if (request.getTimeout() != null) {
            return request.getTimeout();
        }

        return parameters.getTimeout();
    }
}
