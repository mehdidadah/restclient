package fr.netfit.commons.rest.client.impl.rest;


import fr.netfit.commons.rest.client.RequestSender;
import fr.netfit.commons.rest.client.RestClientParams;
import fr.netfit.commons.rest.client.headers.Headers;
import fr.netfit.commons.rest.client.request.Request;
import fr.netfit.commons.rest.client.response.ResponseImpl;
import fr.netfit.commons.rest.client.response.Response;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.nio.charset.StandardCharsets.UTF_8;

public class RequestSenderImpl implements RequestSender {

    private final RequestSenderMapper senderMapper;
    private final HttpClient httpClient;

    public RequestSenderImpl(RestClientParams params) {
        this(params, createHttpClient(params));
    }

    public RequestSenderImpl(RestClientParams params, HttpClient httpClient) {
        this.senderMapper = new RequestSenderMapper(params);
        this.httpClient = httpClient;
    }

    @Override
    public Response<String> sendRequest(Request request, Headers headers, String body) throws Exception {
        HttpRequest httpRequest = senderMapper.mapToHttpRequest(request, headers, body);
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString(UTF_8));
        return senderMapper.mapToResponse(httpResponse);
    }

    @Override
    public Response<String> ping() throws IOException {
        HttpURLConnection connection = senderMapper.mapToHttpURLConnection();
        connection.setRequestMethod("HEAD");
        return ResponseImpl.<String>builder()
                .status(connection.getResponseCode())
                .build();
    }

    private static HttpClient createHttpClient(RestClientParams params) {
        return HttpClient.newBuilder()
                .connectTimeout(params.getTimeout())
                .build();
    }

}
