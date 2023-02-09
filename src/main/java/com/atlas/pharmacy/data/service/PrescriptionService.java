package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PrescriptionService {

    private final PrescriptionRepository repository;

    public PrescriptionService(PrescriptionRepository repository) {
        this.repository = repository;
    }

    public Optional<Prescription> get(Long id) {
        return repository.findById(id);
    }

    public Prescription update(Prescription entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Prescription> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Prescription> list(Pageable pageable, Specification<Prescription> filter) {
        return repository.findAll(filter, pageable);
    }

    public List<Prescription> findAll() {
        return repository.findAll();
    }

    public List<Prescription> findAll(Sort sort) {
        return repository.findAll(sort);
    }

    public int count() {
        return (int) repository.count();
    }
}
