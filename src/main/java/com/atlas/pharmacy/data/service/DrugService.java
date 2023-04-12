package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Drug;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DrugService {

    private static final String API_URL = "https://health-products.canada.ca/api/drug/activeingredient/?lang=en&type=json";

    private final DrugRepository drugRepository;

    public DrugService(DrugRepository drugRepository) {
        this.drugRepository = drugRepository;
    }

    public List<Drug> fetchDrugs() {
        RestTemplate restTemplate = new RestTemplate();
        Drug[] drugs = restTemplate.getForObject(API_URL, Drug[].class);
        return Arrays.asList(drugs);
    }

    public List<Drug> searchDrugs(String query) {
        return fetchDrugs().stream()
                .filter(drug -> String.valueOf(drug.getDrug_code()).contains(query) ||
                        drug.getIngredient_name().toLowerCase().contains(query.toLowerCase()))
                .collect(Collectors.toList());
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
