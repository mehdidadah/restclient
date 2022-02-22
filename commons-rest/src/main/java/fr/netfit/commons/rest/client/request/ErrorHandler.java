package fr.netfit.commons.rest.client.request;

import fr.netfit.commons.rest.client.response.Response;
import fr.netfit.commons.service.error.ServiceException;
import org.springframework.lang.NonNull;

/**
 * Handler for HTTP server error
 */
public interface ErrorHandler<T> {

    /**
     * Handle error received from server.
     *
     * @param response response received
     * @return The ServiceException to throw
     */
    @NonNull
    ServiceException handleError(Response<T> response);

    @NonNull
    Class<T> getErrorClass();
}
