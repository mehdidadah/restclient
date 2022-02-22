package fr.netfit.commons.rest.client.request;

import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.Map;

public interface Request {

    String getMethod();

    String getUrl();

    Map<String, Object> getParameters();

    Map<String, String> getHeaders();

    @Nullable
    @SuppressWarnings("squid:S1452")
    ErrorHandler<?> getErrorHandler();

    @Nullable
    Duration getTimeout();

    interface WithBody {
        Object getBody();
    }

    interface WithResponseType<R> {
        Class<R> getResponseType();
    }
}
