package fr.netfit.commons.rest.client.response;

import java.util.Map;

public interface Response<T> {

    int getStatus();

    T getBody();

    Map<String, String> getHeaders();
}
