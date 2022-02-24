package fr.netfit.commons.rest.client.request;

import fr.netfit.commons.rest.client.request.Request.WithBody;
import fr.netfit.commons.rest.client.request.Request.WithResponseType;
import lombok.Builder;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.time.Duration;
import java.util.Map;

@Builder
@Getter
public class PostRequest<T> implements Request, WithBody, WithResponseType<T> {

    @NonNull
    private final String url;

    private final Map<String, Object> params;

    private final Map<String, String> headers;

    @NonNull
    private final Class<T> responseType;

    private final Object body;

    private final ErrorHandler<String> errorHandler;

    private final Duration timeout;

    public String getMethod() {
        return "POST";
    }
}
