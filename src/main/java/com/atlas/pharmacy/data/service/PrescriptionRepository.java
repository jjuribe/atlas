package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Prescription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrescriptionRepository extends
        JpaRepository<Prescription, Long>,
        JpaSpecificationExecutor<Prescription> {
}
