package ru.standardsolutions;

import com.fasterxml.jackson.annotation.JsonCreator;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Ответ на запрос выборки данных.
 *
 * @param <T> тип запрашиваемой сущности.
 */
@Data
public class FetchResponse<T> {

    @Valid
    @Size(max = 1024, message = "Массив не может содержать более {max} элементов")
    @Schema(description = "Содержимое страницы")
    private final List<T> content;

    @NotNull
    @Min(value = 1, message = "Номер страницы не может быть меньше {value}")
    @Max(value = Integer.MAX_VALUE, message = "Номер страницы не может быть больше {value}")
    @Schema(description = "Номер страницы, начинается с 1")
    private final Integer pageNumber;

    @NotNull
    @Min(value = 0, message = "Размер страницы не может быть меньше {value}")
    @Max(value = 1000, message = "Размер страницы не может быть больше {value}")
    @Schema(description = "Размер страницы")
    private final Integer pageSize;

    @NotNull
    @Min(value = 0, message = "Количество ресурсов на странице не может быть меньше {value}")
    @Max(value = 1000, message = "Количество ресурсов на странице не может быть больше {value}")
    @Schema(description = "Количество ресурсов на странице")
    private final Integer numberOfElements;

    @NotNull
    @Min(value = 1, message = "Индекс первого ресурса на странице не может быть меньше {value}")
    @Max(value = Integer.MAX_VALUE, message = "Индекс первого ресурса на странице не может быть больше {value}")
    @Schema(description = "Индекс первого ресурса на странице, начинается с 1")
    private final Long offset;

    @NotNull
    @Schema(description = "Признак первой страницы")
    private final Boolean first;

    @NotNull
    @Schema(description = "Признак последней страницы")
    private final Boolean last;

    @NotNull
    @Min(value = 0, message = "Количество страниц не может быть меньше {value}")
    @Max(value = Integer.MAX_VALUE, message = "Количество страниц не может быть больше {value}")
    @Schema(description = "Количество страниц")
    private final Integer totalPages;

    @NotNull
    @Min(value = 0, message = "Количество ресурсов в коллекции не может быть меньше {value}")
    @Max(value = Integer.MAX_VALUE, message = "Количество ресурсов в коллекции не может быть больше {value}")
    @Schema(description = "Количество ресурсов в коллекции")
    private final Long totalElements;

    public FetchResponse(Page<?> page, List<T> content) {
        this.content = content;
        this.pageNumber = page.getNumber() + 1;
        this.pageSize = page.getSize();
        this.numberOfElements = page.getNumberOfElements();
        this.offset = page.getPageable().getOffset() + 1;
        this.first = page.isFirst();
        this.last = page.isLast();
        this.totalPages = page.getTotalPages();
        this.totalElements = page.getTotalElements();
    }

    @JsonCreator
    public FetchResponse(List<T> content, Integer pageNumber, Integer pageSize, Integer numberOfElements, Long offset,
                         Boolean first, Boolean last, Integer totalPages, Long totalElements) {
        this.content = content;
        this.pageNumber = pageNumber;
        this.pageSize = pageSize;
        this.numberOfElements = numberOfElements;
        this.offset = offset;
        this.first = first;
        this.last = last;
        this.totalPages = totalPages;
        this.totalElements = totalElements;
    }
}
