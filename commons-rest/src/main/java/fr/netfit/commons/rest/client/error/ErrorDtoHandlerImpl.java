package fr.netfit.commons.rest.client.error;

import fr.netfit.commons.rest.client.request.ErrorHandler;
import fr.netfit.commons.rest.client.response.Response;
import fr.netfit.commons.service.error.ErrorDto;
import fr.netfit.commons.service.error.ErrorEnum;
import fr.netfit.commons.service.error.ServiceException;
import org.springframework.lang.NonNull;

public class ErrorDtoHandlerImpl implements ErrorHandler<ErrorDto> {

    /**
     * Handle error received from server.
     *
     * @param response response received
     * @return The ServiceException to throw
     */
    @Override
    @NonNull
    public ServiceException handleError(Response<ErrorDto> response) {
        var errorDto = response.getBody();
        var errorEnum = ErrorEnum.resolve(errorDto.status());
        return new ServiceException(errorEnum);
    }

    @Override
    @NonNull
    public Class<ErrorDto> getErrorClass() {
        return ErrorDto.class;
    }
}
