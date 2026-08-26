package com.peakprogress.backend.training.bouldering;

import com.peakprogress.backend.training.TrainingSession;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "bouldering_grade_results")
public class BoulderingGradeResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "session_id", nullable = false)
    private TrainingSession session;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 4)
    private BoulderingGrade grade;

    @Column(name = "attempted_count", nullable = false)
    private int attemptedCount;

    @Column(name = "completed_count", nullable = false)
    private int completedCount;

    protected BoulderingGradeResult() {
    }

    public BoulderingGradeResult(
            TrainingSession session,
            BoulderingGrade grade,
            int attemptedCount,
            int completedCount
    ) {
        this.session = session;
        this.grade = grade;
        this.attemptedCount = attemptedCount;
        this.completedCount = completedCount;
    }

    public void update(
            BoulderingGrade grade,
            int attemptedCount,
            int completedCount
    ) {
        this.grade = grade;
        this.attemptedCount = attemptedCount;
        this.completedCount = completedCount;
    }

    public Long getId() {
        return id;
    }

    public Long getSessionId() {
        return session.getId();
    }

    public BoulderingGrade getGrade() {
        return grade;
    }

    public int getAttemptedCount() {
        return attemptedCount;
    }

    public int getCompletedCount() {
        return completedCount;
    }
}