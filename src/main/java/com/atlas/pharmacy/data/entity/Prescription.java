package com.atlas.pharmacy.data.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Optional;

@Entity
@Getter
@Setter
public class Prescription extends AbstractEntity {

    private static final double DISPENSING_FEE = 9.99;
    private static final double ONTARIO_HST = 0.13;

    LocalDate dispenseDate;
    private String frequency; // ex: take 1 tablet by mouth once a day.
    private double quantity;
    private int refills;
    private int daySupplyDuration;
    @ManyToOne(fetch = FetchType.LAZY)
    private Patient patient;
    @ManyToOne(fetch = FetchType.LAZY)
    private Drug drug;
    @ManyToOne(fetch = FetchType.LAZY)
    private Prescriber prescriber;

    /**
     * Get total cost of the prescription.
     * Number of pills multiplied by the drug unit cost, multiplied by the tax percentage, plus the dispensing fee.
     *
     * @return Total cost of the prescription, otherwise an empty optional.
     */
    public Optional<Double> getTotalCost() {
        if (quantity < 1 || drug == null) return Optional.empty();
        return Optional.of((quantity * drug.getUnitCost() * ONTARIO_HST) + DISPENSING_FEE);
    }

    /**
     * Get the actual number of days left to go on the prescription.
     *
     * @return Days remaining on the prescription, otherwise an empty optional.
     */
    public Optional<Long> getActualDaysRemaining() {
        return getNextRefillDate()
                .map(next -> next.toEpochDay() - LocalDate.now().toEpochDay());
    }

    /**
     * Get the next refill date.
     * Dispense date plus the total day supply duration.
     *
     * @return Next refill date, otherwise an empty optional.
     */
    public Optional<LocalDate> getNextRefillDate() {
        if (refills < 1 || dispenseDate == null || daySupplyDuration < 1) return Optional.empty();
        return Optional.of(dispenseDate.plusDays(daySupplyDuration));
    }
}
