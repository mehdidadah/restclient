package fr.netfit.commons.rest.client;


import fr.netfit.commons.rest.client.headers.Headers;
import fr.netfit.commons.rest.client.request.Request;
import fr.netfit.commons.rest.client.response.Response;

import java.io.IOException;

public interface RequestSender {

    Response<String> sendRequest(Request request, Headers headers, String body) throws Exception;

    Response<String> ping() throws IOException;
}
