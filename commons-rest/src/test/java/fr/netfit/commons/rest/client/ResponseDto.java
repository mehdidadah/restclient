package fr.netfit.commons.rest.client;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResponseDto {

    private String name;
    private Integer age;
    private boolean status;
}
