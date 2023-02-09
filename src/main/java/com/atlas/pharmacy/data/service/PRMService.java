package com.atlas.pharmacy.data.service;

import lombok.Getter;
import org.springframework.stereotype.Service;

@Service
@Getter
public class PRMService {

    private final PatientService patientService;
    private final DrugService drugService;
    private final PrescriberService prescriberService;
    private final PrescriptionService prescriptionService;

    public PRMService(
            PatientService patientService,
            DrugService drugService,
            PrescriberService prescriberService,
            PrescriptionService prescriptionService
    ) {
        this.patientService = patientService;
        this.drugService = drugService;
        this.prescriberService = prescriberService;
        this.prescriptionService = prescriptionService;
    }

}
