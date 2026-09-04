package com.peakprogress.backend.goal;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TrainingGoalResponse(
        Long id,
        String title,
        GoalMetric metric,
        BigDecimal targetValue,
        String exerciseName,
        LocalDate targetDate,
        LocalDate createdAt,
        BigDecimal currentValue,
        BigDecimal progressPercent,
        boolean completed
) {

    public static TrainingGoalResponse from(
            TrainingGoal goal,
            TrainingGoalProgress progress
    ) {
        return new TrainingGoalResponse(
                goal.getId(),
                goal.getTitle(),
                goal.getMetric(),
                goal.getTargetValue(),
                goal.getExerciseName(),
                goal.getTargetDate(),
                goal.getCreatedAt(),
                progress.currentValue(),
                progress.progressPercent(),
                progress.completed()
        );
    }
}