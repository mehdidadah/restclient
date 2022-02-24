package fr.netfit.commons.service.error;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ErrorFactory {

    public ErrorDto createError(ErrorEnum errorEnum, @Nullable String context) {
        return new ErrorDto(errorEnum.getResponseStatus().toString(), errorEnum.getMessage(), context, null);
    }
}
