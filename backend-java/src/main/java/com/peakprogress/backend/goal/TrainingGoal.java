package com.peakprogress.backend.goal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "training_goals")
public class TrainingGoal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 64)
    private GoalMetric metric;

    @Column(
            name = "target_value",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal targetValue;

    @Column(name = "exercise_name", length = 120)
    private String exerciseName;

    @Column(name = "target_date")
    private LocalDate targetDate;

    @Column(name = "created_at", nullable = false)
    private LocalDate createdAt;

    protected TrainingGoal() {
    }

    public TrainingGoal(
            String title,
            GoalMetric metric,
            BigDecimal targetValue,
            String exerciseName,
            LocalDate targetDate
    ) {
        update(
                title,
                metric,
                targetValue,
                exerciseName,
                targetDate
        );
    }

    public void update(
            String title,
            GoalMetric metric,
            BigDecimal targetValue,
            String exerciseName,
            LocalDate targetDate
    ) {
        this.title = title.trim();
        this.metric = metric;
        this.targetValue = targetValue;
        this.exerciseName = normalizeExerciseName(exerciseName);
        this.targetDate = targetDate;
    }

    @PrePersist
    void assignCreatedAt() {
        if (createdAt == null) {
            createdAt = LocalDate.now();
        }
    }

    private String normalizeExerciseName(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public GoalMetric getMetric() {
        return metric;
    }

    public BigDecimal getTargetValue() {
        return targetValue;
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public LocalDate getTargetDate() {
        return targetDate;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}