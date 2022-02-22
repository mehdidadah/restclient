package fr.netfit.commons.rest.client.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

import static lombok.AccessLevel.PRIVATE;

@Builder
@Getter
@AllArgsConstructor(access = PRIVATE)
public class ResponseImpl<T> implements Response<T> {

    private final int status;

    private final T body;

    private final Map<String, String> headers;
}
