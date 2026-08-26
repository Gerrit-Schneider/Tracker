package com.peakprogress.backend.training.gym;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;

import com.peakprogress.backend.training.TrainingSession;

@Entity
@Table(name = "strength_exercises")
public class StrengthExercise {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private TrainingSession session;

    @Column(name = "exercise_name", nullable = false, length = 100)
    private String exerciseName;

    @Column(name = "exercise_order", nullable = false)
    private int exerciseOrder;

    @OneToMany(
            mappedBy = "exercise",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @OrderBy("setNumber ASC")
    private List<StrengthSet> sets = new ArrayList<>();

    protected StrengthExercise() {
    }

    public StrengthExercise(
            TrainingSession session,
            String exerciseName,
            int exerciseOrder
    ) {
        this.session = session;
        this.exerciseName = exerciseName;
        this.exerciseOrder = exerciseOrder;
    }

    public void update(String exerciseName, int exerciseOrder) {
        this.exerciseName = exerciseName;
        this.exerciseOrder = exerciseOrder;
    }

    public void addSet(StrengthSet strengthSet) {
        strengthSet.assignTo(this);
        sets.add(strengthSet);
    }

    public void clearSets() {
        sets.clear();
    }

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return session.getId();
    }

    public String getExerciseName() {
        return exerciseName;
    }

    public int getExerciseOrder() {
        return exerciseOrder;
    }

    public List<StrengthSet> getSets() {
        return List.copyOf(sets);
    }
}