package ru.standardsolutions;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.util.CollectionUtils;
import ru.standardsolutions.dto.generic.GenericFetchRequestDto;
import ru.standardsolutions.request.FetchRequest;
import ru.standardsolutions.request.FetchRequestBuilder;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FetchManager {

    public static <E, T, R extends GenericFetchRequestDto<?, ?>> FetchResponse<T> performFetch(
            JpaSpecificationExecutor<E> repository,
            Function<E, T> mapper,
            R request,
            Consumer<FetchRequestBuilder> filterConfigurator
    ) {
        FetchRequestBuilder fetchRequestBuilder = new FetchRequestBuilder();

        if (!CollectionUtils.isEmpty(request.getSort())) {
            request.getSort().forEach(sortField -> fetchRequestBuilder
                    .sort(sortField.getField().getValue(), sortField.getDirection().getValue()));
        }

        filterConfigurator.accept(fetchRequestBuilder);

        final FetchRequest fetchRequest = fetchRequestBuilder
                .page(request.getPage().getNumber(), request.getPage().getSize())
                .build();

        final Page<E> entityPage = repository.findAll(fetchRequest.toSpecification(), fetchRequest.toPageable());

        final List<T> dtoList = entityPage.getContent().stream()
                .map(mapper)
                .toList();

        return new FetchResponse<>(entityPage, dtoList);
    }

    public static <T, R extends GenericFetchRequestDto<?, ?>> FetchResponse<T> emptyFetch(R request) {
        return new FetchResponse<>(
                Page.empty(org.springframework.data.domain.PageRequest.of(
                        0,
                        request.getPage().getSize()
                )),
                Collections.emptyList()
        );
    }

}
