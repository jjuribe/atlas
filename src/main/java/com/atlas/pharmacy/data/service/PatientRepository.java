package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PatientRepository extends
        JpaRepository<Patient, Long>,
        JpaSpecificationExecutor<Patient> {

    @Query("SELECT p.occupation, COUNT(p) FROM Patient p GROUP BY p.occupation")
    List<Object[]> countPatientsByOccupation();
}
