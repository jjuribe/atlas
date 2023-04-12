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

    private String dosage_unit;
    private String dosage_value;
    private int drug_code;
    private String ingredient_name;
    private String strength;
    private String strength_unit;

    @OneToMany
    private List<Prescription> prescriptions = new ArrayList<>();

    public Optional<String> getCompleteName() {
        return Optional.of(String.format("%d %s %s %s", drug_code, ingredient_name, dosage_unit, dosage_value));
    }

    @Override
    public String toString() {
        return ingredient_name.concat(" ")
                .concat(dosage_unit)
                .concat(" ")
                .concat(dosage_value)
                .concat(" ")
                .concat(strength)
                .concat(" ")
                .concat(strength_unit);
    }
}
