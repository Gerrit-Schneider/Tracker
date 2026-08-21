package com.peakprogress.backend.training;

import java.time.LocalDate;

public record TrainingSessionResponse(
        Long id,
        TrainingType type,
        LocalDate trainingDate,
        int durationMinutes,
        String notes
) {

    public static TrainingSessionResponse from(
            TrainingSession session
    ) {
        return new TrainingSessionResponse(
                session.getId(),
                session.getType(),
                session.getTrainingDate(),
                session.getDurationMinutes(),
                session.getNotes()
        );
    }
}