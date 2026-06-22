package com.efmbank.cards.util;

import lombok.experimental.UtilityClass;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Set;

@UtilityClass
public class PageableSanitizer {

    public static Pageable sanitize(Pageable pageable, Set<String> allowedSortProperties) {
        if (pageable == null || pageable.isUnpaged()) {
            return Pageable.unpaged();
        }

        List<Sort.Order> allowedOrders = pageable.getSort().stream()
            .filter(order -> allowedSortProperties.contains(order.getProperty()))
            .toList();

        Sort sort = allowedOrders.isEmpty()
            ? Sort.unsorted()
            : Sort.by(allowedOrders);

        return PageRequest.of(
            pageable.getPageNumber(),
            pageable.getPageSize(),
            sort
        );
    }
}
