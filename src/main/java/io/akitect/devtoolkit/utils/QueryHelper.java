package io.akitect.devtoolkit.utils;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class QueryHelper<T> {

    /**
     * Create a JPA Specification based on query filters.
     *
     * @param filters list of query filters
     * @return specification
     */
    public static <T> Specification<T> createSpecification(List<QueryFilter> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (QueryFilter filter : filters) {
                switch (filter.operation()) {
                    case EQUAL -> predicates.add(criteriaBuilder.equal(root.get(filter.field()), filter.value()));
                    case LIKE -> predicates.add(criteriaBuilder.like(root.get(filter.field()), "%" + filter.value() + "%"));
                    case GREATER_THAN -> predicates.add(criteriaBuilder.greaterThan(root.get(filter.field()), (Comparable) filter.value()));
                    case LESS_THAN -> predicates.add(criteriaBuilder.lessThan(root.get(filter.field()), (Comparable) filter.value()));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public enum Operation {
        EQUAL, LIKE, GREATER_THAN, LESS_THAN
    }

    /**
     * QueryFilter record for holding filter information.
     */
    public static record QueryFilter(String field, Object value, Operation operation) {
    }
}
