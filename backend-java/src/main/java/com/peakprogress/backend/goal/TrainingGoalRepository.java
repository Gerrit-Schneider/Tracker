package com.peakprogress.backend.goal;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainingGoalRepository
        extends JpaRepository<TrainingGoal, Long> {

    List<TrainingGoal> findAllByOrderByCreatedAtDescIdDesc();
}