package com.atlas.pharmacy.data.service;

import com.atlas.pharmacy.data.entity.Prescriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface PrescriberRepository extends
        JpaRepository<Prescriber, Long>,
        JpaSpecificationExecutor<Prescriber> {
}
