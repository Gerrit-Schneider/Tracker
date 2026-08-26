package com.peakprogress.backend.training.running;

import com.peakprogress.backend.training.TrainingSession;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "running_details")
public class RunningDetails {

    @Id
    @Column(name = "session_id")
    private Long sessionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "session_id")
    private TrainingSession session;

    @Enumerated(EnumType.STRING)
    @Column(name = "run_type", nullable = false)
    private RunType runType;

    @Column(name = "distance_meters", nullable = false)
    private int distanceMeters;

    @Column(name = "elapsed_seconds", nullable = false)
    private int elapsedSeconds;

    @Column(name = "average_heart_rate")
    private Integer averageHeartRate;

    @Column(name = "max_heart_rate")
    private Integer maxHeartRate;

    protected RunningDetails() {
    }

    public RunningDetails(
            TrainingSession session,
            RunType runType,
            int distanceMeters,
            int elapsedSeconds,
            Integer averageHeartRate,
            Integer maxHeartRate
    ) {
        this.session = session;
        this.runType = runType;
        this.distanceMeters = distanceMeters;
        this.elapsedSeconds = elapsedSeconds;
        this.averageHeartRate = averageHeartRate;
        this.maxHeartRate = maxHeartRate;
    }

    public void update(
            RunType runType,
            int distanceMeters,
            int elapsedSeconds,
            Integer averageHeartRate,
            Integer maxHeartRate
    ) {
        this.runType = runType;
        this.distanceMeters = distanceMeters;
        this.elapsedSeconds = elapsedSeconds;
        this.averageHeartRate = averageHeartRate;
        this.maxHeartRate = maxHeartRate;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public RunType getRunType() {
        return runType;
    }

    public int getDistanceMeters() {
        return distanceMeters;
    }

    public int getElapsedSeconds() {
        return elapsedSeconds;
    }

    public int getPaceSecondsPerKilometer() {
        return (int) Math.round(
                (double) elapsedSeconds * 1000 / distanceMeters
        );
    }

    public Integer getAverageHeartRate() {
        return averageHeartRate;
    }

    public Integer getMaxHeartRate() {
        return maxHeartRate;
    }
}