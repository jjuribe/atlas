package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Prescriber;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrescriberService {

    private final PrescriberRepository repository;

    public PrescriberService(PrescriberRepository repository) {
        this.repository = repository;
    }

    public Optional<Prescriber> get(Long id) {
        return repository.findById(id);
    }

    public Prescriber update(Prescriber entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Prescriber> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Prescriber> list(Pageable pageable, Specification<Prescriber> filter) {
        return repository.findAll(filter, pageable);
    }

    public List<Prescriber> findAll() {
        return repository.findAll();
    }

    public int count() {
        return (int) repository.count();
    }
}
