package com.atlas.pharmacy.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.OneToMany;
import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Fetch;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
    @OneToMany(fetch = FetchType.EAGER)
    private List<Prescription> prescriptions = new ArrayList<>();

    public Optional<String> getFullName() {
        if (firstName == null || lastName == null) {
            return Optional.empty();
        }
        else {
            return Optional.of(String.format("%s %s", firstName, lastName));
        }
    }
}
