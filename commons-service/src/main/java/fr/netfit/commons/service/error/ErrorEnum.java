package fr.netfit.commons.service.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.util.Arrays;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.GATEWAY_TIMEOUT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;


@Slf4j
@Getter
@RequiredArgsConstructor
public enum ErrorEnum {


    // -- ERREUR GENERIQUE --
    APPLICATION_ERROR(INTERNAL_SERVER_ERROR, Constants.GENERIC_ERROR_MESSAGE, ErrorCodeType.TECHNICAL),
    DATA_INTEGRITY_VIOLATION(INTERNAL_SERVER_ERROR, Constants.GENERIC_ERROR_MESSAGE, ErrorCodeType.TECHNICAL),
    APPLICATION_TIMEOUT(GATEWAY_TIMEOUT, Constants.GENERIC_ERROR_MESSAGE, ErrorCodeType.TECHNICAL),

    // -- ERREUR CLIENT --
    INVALID_REQUEST(BAD_REQUEST, "La requete envoyee n'est pas valide", ErrorCodeType.FUNCTIONAL);

    private static class Constants {
        private static final String GENERIC_ERROR_MESSAGE = "Une erreur est survenue";
    }

    private final HttpStatus responseStatus;
    private final String message;
    private final ErrorCodeType errorCodeType;

    public static ErrorEnum resolve(String status) {
        return Arrays.stream(values())
                .filter(rs -> Integer.parseInt(status) == rs.getResponseStatus().value())
                .findFirst()
                .orElseGet(() -> {
                    log.warn("status inconnu : {}", status);
                    return APPLICATION_ERROR;
                });
    }

    public enum ErrorCodeType {
        TECHNICAL, FUNCTIONAL
    }
}
