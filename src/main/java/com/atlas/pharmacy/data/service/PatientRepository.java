package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PatientRepository extends
        JpaRepository<Patient, Long>,
        JpaSpecificationExecutor<Patient> {
}
