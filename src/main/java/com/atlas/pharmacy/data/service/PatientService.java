package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Patient;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    private final PatientRepository repository;

    public PatientService(PatientRepository repository) {
        this.repository = repository;
    }

    public Optional<Patient> get(Long id) {
        return repository.findById(id);
    }

    public Patient update(Patient entity) {
        return repository.save(entity);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

    public Page<Patient> list(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<Patient> list(Pageable pageable, Specification<Patient> filter) {
        return repository.findAll(filter, pageable);
    }

    public int count() {
        return (int) repository.count();
    }

}
