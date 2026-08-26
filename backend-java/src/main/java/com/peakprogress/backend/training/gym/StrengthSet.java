
package com.peakprogress.backend.training.gym;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "strength_sets")
public class StrengthSet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "exercise_id", nullable = false)
    private StrengthExercise exercise;

    @Column(name = "set_number", nullable = false)
    private int setNumber;

    @Column(name = "repetitions", nullable = false)
    private int repetitions;

    @Column(
            name = "weight_kg",
            nullable = false,
            precision = 6,
            scale = 2
    )
    private BigDecimal weightKg;

    protected StrengthSet() {
    }

    public StrengthSet(
            int setNumber,
            int repetitions,
            BigDecimal weightKg
    ) {
        this.setNumber = setNumber;
        this.repetitions = repetitions;
        this.weightKg = weightKg;
    }

    void assignTo(StrengthExercise exercise) {
        this.exercise = exercise;
    }

    public Long getId() {
        return id;
    }

    public int getSetNumber() {
        return setNumber;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public BigDecimal getWeightKg() {
        return weightKg;
    }

    public BigDecimal getVolumeKg() {
        return weightKg.multiply(BigDecimal.valueOf(repetitions));
    }
}