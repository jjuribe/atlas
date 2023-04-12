package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PrescriptionRepository extends
        JpaRepository<Prescription, Long>,
        JpaSpecificationExecutor<Prescription> {

    // The query selects prescriptions where the patient's full name (concatenated first name and last name)
    // contains the search string, ignoring the case.
    @Query("SELECT p FROM Prescription p WHERE LOWER(CONCAT(p.patient.firstName, ' ', p.patient.lastName)) LIKE LOWER(CONCAT('%', :searchString, '%'))")
    Page<Prescription> findByPatientNameContainingIgnoreCase(@Param("searchString") String searchString, Pageable pageable);

    @Query("SELECT p.drug.ingredient_name, COUNT(p) FROM Prescription p GROUP BY p.drug.ingredient_name")
    List<Object[]> countPrescriptionsByDrug();
}
