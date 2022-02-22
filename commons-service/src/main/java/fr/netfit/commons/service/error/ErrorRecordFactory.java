package fr.netfit.commons.service.error;

import lombok.AllArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class ErrorRecordFactory {

    public ErrorRecord createError(ErrorEnum errorEnum, @Nullable String context) {
        return new ErrorRecord(errorEnum.getResponseStatus().toString(), errorEnum.getMessage(), context, null);
    }
}
