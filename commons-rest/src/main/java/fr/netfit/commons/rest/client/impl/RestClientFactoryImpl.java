package fr.netfit.commons.rest.client.impl;


import com.fasterxml.jackson.databind.ObjectMapper;
import fr.netfit.commons.rest.client.RequestSender;
import fr.netfit.commons.rest.client.RestClient;
import fr.netfit.commons.rest.client.RestClientFactory;
import fr.netfit.commons.rest.client.RestClientParams;
import fr.netfit.commons.rest.client.headers.HeadersService;
import fr.netfit.commons.rest.client.impl.rest.RequestSenderImpl;
import fr.netfit.commons.rest.client.health.StatusHealthIndicator;
import fr.netfit.commons.service.logger.perf.PerfService;
import lombok.AllArgsConstructor;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.cloud.sleuth.Tracer;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class RestClientFactoryImpl implements RestClientFactory {

    private final HealthContributorRegistry registry;
    private final HeadersService headersService;
    private final ObjectMapper objectMapper;
    private final PerfService perfService;
    private final Tracer tracer;

    /**
     * Method for creating a new HTTP client from a configuration
     *
     * @param params http client configuration
     * @return the http client
     */
    @Override
    public RestClient createRestClient(RestClientParams params) {
        return createRestClient(params, defaultRequestSender(params));
    }

    /**
     *
     * Factory to use for tests or for a particular configuration of HttpClient
     *
     * @param params    The params to use when creating the RestClient
     * @param requestSender The underlying implementation to use
     * @return A new RestClient
     * @see RestClientFactory#createRestClient(RestClientParams)
     */
    public RestClient createRestClient(RestClientParams params, RequestSender requestSender) {
        RestClientImpl restClient = new RestClientImpl(params, headersService, tracer, objectMapper, perfService, requestSender);
        registry.registerContributor(params.getServiceName(), new StatusHealthIndicator(restClient));
        return restClient;
    }

    private RequestSender defaultRequestSender(RestClientParams params) {
        return new RequestSenderImpl(params);
    }

}
