package com.peakprogress.backend.training.gym;

import java.math.BigDecimal;
import java.util.List;

public record StrengthExerciseResponse(
        Long id,
        String exerciseName,
        int exerciseOrder,
        List<StrengthSetResponse> sets,
        BigDecimal volumeKg
) {

    public static StrengthExerciseResponse from(
            StrengthExercise exercise
    ) {
        List<StrengthSetResponse> setResponses =
                exercise.getSets().stream()
                        .map(StrengthSetResponse::from)
                        .toList();

        BigDecimal volumeKg = exercise.getSets().stream()
                .map(StrengthSet::getVolumeKg)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new StrengthExerciseResponse(
                exercise.getId(),
                exercise.getExerciseName(),
                exercise.getExerciseOrder(),
                setResponses,
                volumeKg
        );
    }
}