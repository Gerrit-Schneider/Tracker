package com.peakprogress.backend.training;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "training_sessions")
public class TrainingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TrainingType type;

    @Column(nullable = false)
    private LocalDate trainingDate;

    @Column(nullable = false)
    private int durationMinutes;

    @Column(length = 1000)
    private String notes;

    protected TrainingSession() {
    }

    public TrainingSession(
            TrainingType type,
            LocalDate trainingDate,
            int durationMinutes,
            String notes
    ) {
        this.type = type;
        this.trainingDate = trainingDate;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
    }

public void update(
        TrainingType type,
        LocalDate trainingDate,
        int durationMinutes,
        String notes
) {
    this.type = type;
    this.trainingDate = trainingDate;
    this.durationMinutes = durationMinutes;
    this.notes = notes;
}

    public Long getId() {
        return id;
    }

    public TrainingType getType() {
        return type;
    }

    public LocalDate getTrainingDate() {
        return trainingDate;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public String getNotes() {
        return notes;
    }
}