package com.atlas.pharmacy.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Entity
@Getter
@Setter
public class Patient extends AbstractEntity {

    private String firstName;
    private String lastName;
    @Email
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private String occupation;
    private String role;

    private String streetAddress;
    private String province;
    private String city;
    private String postalCode;

    private String allergy;
    private String healthCardId;
    @OneToMany
    private List<Prescription> prescriptions;
}
