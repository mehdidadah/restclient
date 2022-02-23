package fr.netfit.commons.rest.client;

import fr.netfit.commons.rest.client.error.ErrorRecordHandler;
import fr.netfit.commons.rest.client.request.ErrorHandler;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import org.springframework.lang.NonNull;

import java.net.URI;
import java.time.Duration;

@Builder
@Getter
public class RestClientParameters {

    @NonNull
    private final String serviceName;

    @NonNull
    private final URI rootUri;

    @NonNull
    private final Duration timeout;

    @Default
    private final boolean headersForwardingEnabled = true;

    @Default
    @NonNull
    private final ErrorHandler<?> defaultErrorHandler = new ErrorRecordHandler();

}
