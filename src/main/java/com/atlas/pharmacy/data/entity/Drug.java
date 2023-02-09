package com.atlas.pharmacy.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Getter
@Setter
public class Drug extends AbstractEntity {

    private long drugIdentificationNumber;
    private String dosage;
    private String manufacturer;
    private String brandName;
    private String genericName;
    private String description;
    private String form;
    private double unitCost;
    private double stockQuantity;
    @OneToMany
    private List<Prescription> prescriptions = new ArrayList<>();

    public Optional<String> getCompleteName() {
        return Optional.of(String.format("%d %s %s", drugIdentificationNumber, brandName, genericName));
    }
}
