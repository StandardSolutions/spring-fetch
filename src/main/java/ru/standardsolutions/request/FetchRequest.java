package ru.standardsolutions.request;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Order;
import org.springframework.data.jpa.domain.Specification;
import ru.standardsolutions.FetchRequestParser;
import ru.standardsolutions.FetchSpecification;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.data.domain.Sort.Direction.ASC;

/**
 * Запрос на получение данных с фильтрацией, сортировкой и пагинацией.
 * <p>
 * Если параметры сортировки и фильтрации не переданы, то не применяются.
 * По умолчанию пагинация начинается со страницы 1, с размером страницы 100.
 */
@Getter
@ToString
@EqualsAndHashCode
@Schema(description = "Запрос на получение данных с фильтрацией, сортировкой и пагинацией",
        additionalProperties = Schema.AdditionalPropertiesValue.TRUE
)
public class FetchRequest {

    @Valid
    @Size(max = 255)
    @ArraySchema(schema = @Schema(description = "Фильтр", nullable = true),
            arraySchema = @Schema(description = "Список фильтров", nullable = true)
    )
    private final List<FilterRequest> filters;

    @Valid
    @Size(max = 255)
    @ArraySchema(schema = @Schema(description = "Параметр сортировки"),
            arraySchema = @Schema(description = "Список параметров сортировки", nullable = true)
    )
    private final List<SortRequest> sort;

    @Valid
    @Schema(description = "Параметры страницы", nullable = true)
    private final PageRequest page;

    @JsonCreator
    public FetchRequest(@JsonProperty("filters") List<FilterRequest> filters,
                        @JsonProperty("sort") List<SortRequest> sort,
                        @JsonProperty("page") ru.standardsolutions.request.PageRequest page) {
        this.filters = filters == null ? List.of() : filters;
        this.sort = sort == null ? List.of() : sort;
        this.page = page == null ? new PageRequest(1, 100) : page;
    }

    public FetchRequest(String filters, String sort, Integer pageNumber, Integer pageSize) {
        this.filters = filters == null ? List.of() : FetchRequestParser.parseFilter(filters);
        this.sort = sort == null ? List.of() : FetchRequestParser.parseSort(sort);
        this.page = new PageRequest(pageNumber == null ? 1 : pageNumber, pageSize == null ? 100 : pageSize);
    }

    @JsonIgnore
    public Pageable toPageable() {
        return toPageable(page.getNumber(), page.getSize());
    }

    @JsonIgnore
    public Pageable toPageable(Integer pageNumber, Integer pageSize) {
        List<Order> orders = new ArrayList<>();
        for (SortRequest sortPart : sort) {
            Sort.Direction direction = Sort.Direction.fromString(sortPart.getDirection());
            Order order = direction == ASC ? Order.asc(sortPart.getField()) : Order.desc(sortPart.getField());
            orders.add(order);
        }
        return org.springframework.data.domain.PageRequest.of(pageNumber - 1, pageSize, Sort.by(orders));
    }

    @JsonIgnore
    public <T> Specification<T> toSpecification() {
        return new FetchSpecification<>(this);
    }
}