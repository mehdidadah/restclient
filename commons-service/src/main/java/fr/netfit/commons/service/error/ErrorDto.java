package fr.netfit.commons.service.error;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@Schema(name = "Error")
@ApiModel("Error")
@JsonInclude(NON_NULL)
public record ErrorDto(
    @Schema(title = "Error code", required = true, example = "500", description = "See Error enum")
    @ApiModelProperty(value = "Error code", required = true, example = "500", notes = "See Error enum")
    String status,
    @Schema(title = "Error title", required = true, example = "An error has occurred", description = "See Error enum")
    @ApiModelProperty(value = "Error title", required = true, example = "Une erreur est survenue")
    String title,
    @Schema(title = "Error detail", required = true, example = "An error has occurred", description = "See Error enum")
    @ApiModelProperty(value = "Error detail", required = true, example = "Une erreur est survenue")
    String detail,
    @Schema(title = "Error requestId", example = "Optional requestId")
    @ApiModelProperty(value = "Error requestId", example = "Optional requestId")
    String requestId
) { }
