package fr.netfit.commons.rest.client.error;

import fr.netfit.commons.rest.client.request.ErrorHandler;
import fr.netfit.commons.rest.client.response.Response;
import fr.netfit.commons.service.error.ErrorRecord;
import fr.netfit.commons.service.error.ErrorEnum;
import fr.netfit.commons.service.error.ServiceException;
import org.springframework.lang.NonNull;

public class ErrorRecordHandler implements ErrorHandler<ErrorRecord> {

    /**
     * Handle error received from server.
     *
     * @param response response received
     * @return The ServiceException to throw
     */
    @Override
    @NonNull
    public ServiceException handleError(Response<ErrorRecord> response) {
        var errorRecord = response.getBody();
        var errorStatus = ErrorEnum.resolve(errorRecord.getStatus());
        return new ServiceException(errorStatus);
    }

    @Override
    @NonNull
    public Class<ErrorRecord> getErrorClass() {
        return ErrorRecord.class;
    }
}
