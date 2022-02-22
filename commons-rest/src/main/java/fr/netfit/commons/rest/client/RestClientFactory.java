package fr.netfit.commons.rest.client;

import fr.netfit.commons.rest.client.impl.RestClientFactoryImpl;

/**
 * Interface for creating HTTP client
 * A bean implementing this type is expected in the services using this client
 *
 * @see RestClientFactoryImpl
 */
public interface RestClientFactory {

    /**
     * Method for creating a new HTTP client from a configuration
     *
     * @param parameters http client configuration
     * @return the http client
     */
    RestClient createRestClient(RestClientParameters parameters);

}
