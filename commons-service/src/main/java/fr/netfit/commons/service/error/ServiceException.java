package fr.netfit.commons.service.error;

public class ServiceException extends RuntimeException {

    private final ErrorEnum errorEnum;

    public ServiceException(ErrorEnum errorEnum) {
        super(errorEnum.getMessage(), null);
        this.errorEnum = errorEnum;
    }

    public ErrorEnum getError() {
        return errorEnum;
    }
}
