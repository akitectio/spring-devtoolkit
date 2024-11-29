package io.akitect.devtoolkit.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {

    /**
     * Find entities by specification.
     *
     * @param specification the JPA specification
     * @return list of entities
     */
    default List<T> findAllBySpecification(Specification<T> specification) {
        return findAll(specification);
    }
}
