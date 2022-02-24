package fr.netfit.commons.rest.client.request;

import fr.netfit.commons.rest.client.request.Request.WithResponseType;
import lombok.Builder;
import lombok.Getter;
import lombok.Singular;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Builder
@Getter
public class GetRequest<T> implements Request, WithResponseType<T> {

    @NonNull
    private final String url;

    private final Map<String, Object> params = new HashMap<>();

    private final Map<String, String> headers;

    @NonNull
    private final Class<T> responseType;

    private final ErrorHandler<String> errorHandler;

    private final Duration timeout;

    public String getMethod() {
        return "GET";
    }
}
