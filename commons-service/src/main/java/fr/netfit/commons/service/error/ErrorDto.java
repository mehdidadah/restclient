package fr.netfit.commons.service.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@ApiModel("Error")
@JsonInclude(NON_NULL)
public record ErrorDto(
    String status,
    String title,
    String detail,
    String requestId
) { }
