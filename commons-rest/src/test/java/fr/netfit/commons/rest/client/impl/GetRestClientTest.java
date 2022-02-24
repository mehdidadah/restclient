package fr.netfit.commons.rest.client.impl;

import fr.netfit.commons.rest.client.ResponseDto;
import fr.netfit.commons.rest.client.RestClientFactory;
import fr.netfit.commons.rest.client.RestClientParams;
import fr.netfit.commons.rest.client.health.StatusHealthIndicator;
import fr.netfit.commons.rest.client.request.ErrorHandler;
import fr.netfit.commons.rest.client.request.GetRequest;
import fr.netfit.commons.rest.client.response.Response;
import fr.netfit.commons.service.CommonsConfiguration;
import fr.netfit.commons.service.error.ServiceException;
import fr.netfit.commons.service.logger.perf.Perf;
import fr.netfit.commons.service.logger.perf.PerfService;
import fr.netfit.commons.service.request.RequestHeadersService;
import fr.netfit.commons.service.request.RequestIdService;
import org.junit.jupiter.api.*;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.socket.PortFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.HealthContributorRegistry;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.net.URI;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;

import static fr.netfit.commons.service.error.ErrorEnum.APPLICATION_ERROR;
import static fr.netfit.commons.service.error.ErrorEnum.APPLICATION_TIMEOUT;
import static fr.netfit.commons.service.request.RequestIdService.HEADER_REQUEST_ID;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

/**
 * Integration testing for RestClient and RestClientFactory implementations
 */

@EnableAutoConfiguration
@SpringBootTest(classes = {CommonsConfiguration.class})
class GetRestClientTest {

    private static ClientAndServer mockServer;
    private static RestClientParams params;
    private static int port;

    @Autowired
    private RestClientFactory factory;

    @MockBean
    private HealthContributorRegistry registry;

    @MockBean
    private RequestIdService requestIdService;

    @MockBean
    private PerfService perfService;

    @MockBean
    private RequestHeadersService requestHeadersService;

    @Mock
    private Perf perf;

    @Mock
    private ErrorHandler<String> errorHandler;

    @Captor
    private ArgumentCaptor<Response<String>> responseCaptor;

    @BeforeAll
    static void start() {
        port = PortFactory.findFreePort();
        mockServer = ClientAndServer.startClientAndServer(port);
        params = RestClientParams.builder()
                .rootUri(URI.create("http://localhost:" + port))
                .serviceName("MockTest")
                .timeout(Duration.ofSeconds(1))
                .build();
    }

    @BeforeEach
    void setUp() {
        mockServer.reset();
        when(requestIdService.getRequestId()).thenReturn("X_REQUEST_ID_VALUE");
        when(requestHeadersService.getTechnicalHeadersMap()).thenReturn(
                Map.of("X-FORWARDED-FOR", List.of("127.0.0.1", "127.0.0.2", "127.0.0.3")));

        when(perfService.startPerf(anyString(), anyString())).thenReturn(perf);
        when(errorHandler.getErrorClass()).thenReturn(String.class);
    }

    @AfterAll
    static void teardown() {
        mockServer.stop();
    }

    @Test
    @DisplayName("Get - Mock response 200")
    void get_response_200() {
        // GIVEN
        mockServer.when(
                        HttpRequest.request()
                                .withMethod("GET")
                                .withPath("/api")
                                .withHeader(HEADER_REQUEST_ID, "X_REQUEST_ID_VALUE")
                                .withHeader("X-FORWARDED-FOR", "127.0.0.1", "127.0.0.2", "127.0.0.3")
                                .withHeader("HEADER-CUSTOM", "Une valeur de header"))
                .respond(
                        HttpResponse.response()
                                .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                                .withBody("{\"name\":\"Mehdi\", \"age\":31,\"status\":true}"));

        var restClient = factory.createRestClient(params);

        var request = GetRequest.<ResponseDto>builder()
                .url("/api")
                .headers(Map.of("HEADER-CUSTOM", "Une valeur de header"))
                .responseType(ResponseDto.class)
                .build();

        // WHEN
        var result = restClient.get(request);

        // THEN
        assertThat(result).get().satisfies(actual -> {
            assertThat(actual.name()).isEqualTo("Mehdi");
            assertThat(actual.status()).isTrue();
            assertThat(actual.age()).isEqualTo(31);
        });

        verify(perfService).startPerf("http://localhost:" + port + "/api", "GET");
        verify(perf).addDetails("http.status", 200);
        verify(perfService).ok(perf);
        verify(registry).registerContributor(eq("MockTest"), any(StatusHealthIndicator.class));
    }

    @Test
    @DisplayName("Get - Mock timeout exception")
    void get_Timeout_exception() {
        // GIVEN
        mockServer.when(
                HttpRequest.request()
                    .withMethod("GET")
                    .withPath("/api")
                    .withHeader(HEADER_REQUEST_ID, "X_REQUEST_ID_VALUE")
                    .withHeader("X-FORWARDED-FOR", "127.0.0.1", "127.0.0.2", "127.0.0.3")
                    .withHeader("HEADER-CUSTOM", "Une valeur de header"))
            .respond(
                HttpResponse.response()
                    .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                    .withBody("{\"name\":\"Mehdi\", \"value\":31,\"status\":true}")
                    .withDelay(SECONDS, 2));

        var restClient = factory.createRestClient(params);

        var request = GetRequest.<ResponseDto>builder()
            .url("/api")
            .headers(Map.of("HEADER-CUSTOM", "Une valeur de header"))
            .responseType(ResponseDto.class)
            .build();

        // WHEN
        // THEN
        assertThatExceptionOfType(ServiceException.class)
            .isThrownBy(() -> restClient.get(request))
            .withMessage("Une erreur est survenue")
            .satisfies(e -> assertThat(e.getError()).isEqualTo(APPLICATION_TIMEOUT));

        verify(perfService).startPerf("http://localhost:" + port + "/api", "GET");
        verify(perfService).error(eq(perf), any(HttpTimeoutException.class));
        verify(registry).registerContributor(eq("MockTest"), any(StatusHealthIndicator.class));
    }

    @Test
    @DisplayName("Get - Mock ErrorHandler exception")
    void get_Error_Handler() {
        // GIVEN
        mockServer.when(
                HttpRequest.request()
                    .withMethod("GET")
                    .withPath("/api")
                    .withHeader(HEADER_REQUEST_ID, "X_REQUEST_ID_VALUE")
                    .withHeader("X-FORWARDED-FOR", "127.0.0.1", "127.0.0.2", "127.0.0.3")
                    .withHeader("HEADER-CUSTOM", "Une valeur de header"))
            .respond(
                HttpResponse.response()
                    .withStatusCode(400)
                    .withHeader("Content-Type", APPLICATION_JSON_VALUE)
                    .withBody("{\"status\": 400}"));

        var restClient = factory.createRestClient(params);

        var request = GetRequest.<ResponseDto>builder()
            .url("/api")
            .headers(Map.of("HEADER-CUSTOM", "Une valeur de header"))
            .responseType(ResponseDto.class)
            .errorHandler(errorHandler)
            .build();

        // WHEN
        // THEN
        assertThatExceptionOfType(ServiceException.class)
            .isThrownBy(() -> restClient.get(request))
            .withMessage("Une erreur est survenue")
            .satisfies(e -> assertThat(e.getError()).isEqualTo(APPLICATION_ERROR));

        verify(perfService).startPerf("http://localhost:" + port + "/api", "GET");
        verify(perf).addDetails("http.status", 400);
        verify(perfService).error(perf, "{\"status\": 400}");
        verify(errorHandler).handleError(responseCaptor.capture());

        Response<String> actualResponse = responseCaptor.getValue();
        assertThat(actualResponse).isNotNull();
        assertThat(actualResponse.getStatus()).isEqualTo(400);
        assertThat(actualResponse.getHeaders()).isNotNull();
        assertThat(actualResponse.getBody()).isEqualTo("{\"status\": 400}");

        verify(registry).registerContributor(eq("MockTest"), any(StatusHealthIndicator.class));
    }

}
