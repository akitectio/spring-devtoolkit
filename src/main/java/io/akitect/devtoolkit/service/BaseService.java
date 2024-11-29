package io.akitect.devtoolkit.service;

import io.akitect.devtoolkit.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID> {

    protected abstract BaseRepository<T, ID> getRepository();

    public T save(T entity) {
        return getRepository().save(entity);
    }

    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public Page<T> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    public List<T> findAllBySpecification(Specification<T> specification) {
        return getRepository().findAll(specification);
    }
}
