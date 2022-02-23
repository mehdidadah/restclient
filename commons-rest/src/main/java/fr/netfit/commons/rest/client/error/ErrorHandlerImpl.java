package fr.netfit.commons.rest.client.error;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.netfit.commons.rest.client.request.ErrorHandler;
import fr.netfit.commons.rest.client.response.ResponseImpl;
import fr.netfit.commons.rest.client.response.Response;
import fr.netfit.commons.service.error.ServiceException;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;

import java.util.Objects;

import static fr.netfit.commons.service.error.ErrorEnum.APPLICATION_ERROR;
import static lombok.AccessLevel.PRIVATE;

@Slf4j
@AllArgsConstructor(access = PRIVATE)
public class ErrorHandlerImpl<T> implements ErrorHandler<String> {

    private final ObjectMapper objectMapper;
    private final ErrorHandler<T> errorHandler;

    @SuppressWarnings("unchecked")
    public static ErrorHandler<String> asStringErrorHandler(ObjectMapper objectMapper, ErrorHandler<?> errorHandler) {
        if (Objects.equals(errorHandler.getErrorClass(), String.class)) {
            return (ErrorHandler<String>) errorHandler;
        }
        return new ErrorHandlerImpl<>(objectMapper, errorHandler);
    }

    @Override
    @NonNull
    public ServiceException handleError(Response<String> response) {
        try {
            T error = objectMapper.readValue(response.getBody(), errorHandler.getErrorClass());

            Response<T> newResponse = ResponseImpl.<T>builder()
                .body(error)
                .headers(response.getHeaders())
                .status(response.getStatus())
                .build();

            return errorHandler.handleError(newResponse);

        } catch (JsonProcessingException e) {
            log.error("Une erreur est survenue lors de la deserialization de l'erreur [{}] dans la classe [{}]",
                response.getBody(),
                errorHandler.getErrorClass().getName());

            return new ServiceException(APPLICATION_ERROR);
        }
    }

    @Override
    @NonNull
    public Class<String> getErrorClass() {
        return String.class;
    }

}
