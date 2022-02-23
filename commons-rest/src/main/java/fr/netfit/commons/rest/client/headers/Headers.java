package fr.netfit.commons.rest.client.headers;

import fr.netfit.commons.rest.client.request.Request;
import lombok.Getter;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.springframework.util.CollectionUtils.isEmpty;


@Getter
public class Headers {

    private final Map<String, List<String>> headerMap = new HashMap<>();

    public Headers(Request request) {
        request.getHeaders().forEach(this::addHeader);
    }

    public void setHeader(@NonNull String key, String value) {
        if (value != null) {
            headerMap.put(key, new ArrayList<>(Collections.singleton(value)));
        }
    }

    public void addHeader(@NonNull String key, String value) {
        if (value != null) {
            headerMap.computeIfAbsent(key, k -> new ArrayList<>()).add(value);
        }
    }

    public void addHeader(@NonNull String key, List<String> values) {
        if (!isEmpty(values)) {
            headerMap.computeIfAbsent(key, k -> new ArrayList<>()).addAll(values);
        }
    }

    public void merge(Map<String, List<String>> otherHeaders) {
        if (!isEmpty(otherHeaders)) {
            otherHeaders.forEach(this::addHeader);
        }
    }

    public void forEach(@NonNull BiConsumer<String, String> consumer) {
        headerMap.forEach((key, values) ->
            values.forEach(value -> consumer.accept(key, value)
            ));
    }
}
