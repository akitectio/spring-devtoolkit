package io.akitect.devtoolkit.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PaginationHelper {

    public static Pageable createPageRequest(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    public static <T> PaginationResponse<T> createPaginationResponse(Page<T> pageData) {
        return new PaginationResponse<>(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements()
        );
    }

    public static record PaginationResponse<T>(
            List<T> content,
            int page,
            int size,
            long totalElements
    ) {
    }
}
