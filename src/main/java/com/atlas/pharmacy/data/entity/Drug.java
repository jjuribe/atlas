package com.atlas.pharmacy.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Drug extends AbstractEntity {

    private int drugIdentificationNumber;
    private String brandName;
    private String genericName;
    private String description;
    private String form;
    private double unitCost;
    private double stockQuantity;
    @OneToMany
    private List<Prescription> prescriptions;
}
