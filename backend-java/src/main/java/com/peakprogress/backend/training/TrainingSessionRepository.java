package com.peakprogress.backend.training;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingSessionRepository
        extends JpaRepository<TrainingSession, Long> {

    List<TrainingSession> findAllByOrderByTrainingDateDesc();

    List<TrainingSession> findByTypeOrderByTrainingDateDesc(
            TrainingType type
    );
}