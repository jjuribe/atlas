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
public class Prescriber extends AbstractEntity {

    private long licenseIdentificationNumber;
    private String practice;
    private String firstName;
    private String lastName;
    private String officeAddress;
    private String postalCode;
    @OneToMany
    private List<Prescription> prescriptions = new ArrayList<>();

    public Optional<String> getCompleteName() {
        return Optional.of(String.format("%d %s %s", licenseIdentificationNumber, firstName, lastName));
    }

    @Override
    public String toString() {
        return firstName.concat(" ").concat(lastName);
    }
}
