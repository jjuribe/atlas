package com.atlas.pharmacy.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Entity
@Getter
@Setter
public class Prescriber extends AbstractEntity {

    private long licenseIdentificationNumber;
    private String practice;
    private String firstName;
    private String lastName;
    private String officeAddress;
    private String postalCode;
    @OneToMany
    private List<Prescription> prescriptions;
}
