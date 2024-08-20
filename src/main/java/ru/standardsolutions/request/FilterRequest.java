package ru.standardsolutions.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.util.List;

/**
 * Параметры фильтрации.
 */
@Getter
@ToString
@EqualsAndHashCode
@Schema(description = "Параметры фильтрации")
public class FilterRequest {

    @Size(max = 255, message = "Длина строки должна быть не более {max} символов")
    @Schema(description = "Поле фильтра")
    private final String field;

    @NotNull
    @Size(max = 255, message = "Длина строки должна быть не более {max} символов")
    @Schema(description = "Оператор")
    private final String operator;

    @Size(max = 255, message = "Длина строки должна быть не более {max} символов")
    @Schema(description = "Значение фильтра")
    private final String value;

    @Valid
    @Size(max = 255, message = "Массив не может содержать более {max} элементов")
    @Schema(description = "Набор вложенных фильтров")
    private final List<FilterRequest> filters;

    @JsonCreator
    public FilterRequest(@JsonProperty("field") String field,
                         @JsonProperty("operator") String operator,
                         @JsonProperty("value") String value,
                         @JsonProperty("filters") List<FilterRequest> filters) {
        this.field = field;
        this.operator = operator;
        this.value = value;
        this.filters = filters;
    }
}
