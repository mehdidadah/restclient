package fr.netfit.commons.service.error;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.env.Profiles;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentConversionNotSupportedException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;

import static fr.netfit.commons.service.error.ErrorEnum.APPLICATION_ERROR;
import static fr.netfit.commons.service.error.ErrorEnum.INVALID_REQUEST;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@AllArgsConstructor
@RestControllerAdvice
public class ErrorHandler {

    private final Environment env;
    private final ErrorRecordFactory errorRecordFactory;

    @ExceptionHandler(ServiceException.class)
    public ResponseEntity<ErrorRecord> handleServiceException(ServiceException ex) {
        ErrorEnum errorEnum = ex.getError();

        log.debug("ServiceException recue avec le status {}", errorEnum, ex);

        ErrorRecord errorRecord = errorRecordFactory.createError(errorEnum, "context test");
        return new ResponseEntity<>(errorRecord, errorEnum.getResponseStatus());
    }

    //@Operation(summary = "Bad request (ErrorCode 4000)", description = "Client sent an invalid request to the server")
    @ResponseStatus(BAD_REQUEST)
    @ExceptionHandler({
        MethodArgumentNotValidException.class,
        MethodArgumentTypeMismatchException.class,
        MethodArgumentConversionNotSupportedException.class,
        HttpMessageNotReadableException.class,
        ConstraintViolationException.class,
        HttpMediaTypeNotSupportedException.class,
        MissingServletRequestParameterException.class,
        MissingRequestHeaderException.class,
        ValidationException.class,
        MissingServletRequestPartException.class
    })
    public ErrorRecord handleBadRequest(Exception e) {
        log.debug("Erreur de validation", e);
        return errorRecordFactory.createError(INVALID_REQUEST, getContext(e));
    }

    //@Operation(summary = "Internal server error (ErrorCode 1)", description = "Server handled an unexpected error")
    @ResponseStatus(INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorRecord handleException(Exception ex) {
        log.error("Une exception non geree a ete interceptee : {}", ex.getMessage(), ex);
        return errorRecordFactory.createError(APPLICATION_ERROR, getContext(ex));
    }

    private String getContext(Exception ex) {
        if (env.acceptsProfiles(Profiles.of("debug"))) {
            return ex.getMessage();
        }
        return null;
    }

}
