package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DrugService {

    private final DrugRepository drugRepository;

    public DrugService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    public Optional<Drug> get(Long id) {
        return drugRepository.findById(id);
    }

    public Drug update(Drug entity) {
        return drugRepository.save(entity);
    }

    public void delete(Long id) {
        drugRepository.deleteById(id);
    }

    public Page<Drug> list(Pageable pageable) {
        return drugRepository.findAll(pageable);
    }

    public Page<Drug> list(Pageable pageable, Specification<Drug> filter) {
        return drugRepository.findAll(filter, pageable);
    }

    public List<Drug> findAll() {
        return drugRepository.findAll();
    }

    public int count() {
        return (int) drugRepository.count();
    }
}
