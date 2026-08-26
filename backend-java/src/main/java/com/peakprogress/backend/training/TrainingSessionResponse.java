package com.peakprogress.backend.training;

import com.peakprogress.backend.training.bouldering.BoulderingGradeResult;
import com.peakprogress.backend.training.bouldering.BoulderingGradeResultResponse;
import com.peakprogress.backend.training.running.RunningDetails;
import com.peakprogress.backend.training.running.RunningDetailsResponse;
import com.peakprogress.backend.training.gym.StrengthExercise;
import com.peakprogress.backend.training.gym.StrengthExerciseResponse;

import java.time.LocalDate;
import java.util.List;

public record TrainingSessionResponse(
        Long id,
        TrainingType type,
        LocalDate trainingDate,
        int durationMinutes,
        String notes,
        RunningDetailsResponse runningDetails,
        List<BoulderingGradeResultResponse> boulderingResults,
        List<StrengthExerciseResponse> strengthExercises
) {

    public static TrainingSessionResponse from(
            TrainingSession session
    ) {
        return from(
                session,
                null,
                List.of(),
                List.of()
        );
    }

    public static TrainingSessionResponse from(
            TrainingSession session,
            RunningDetails runningDetails,
            List<BoulderingGradeResult> boulderingResults,
            List<StrengthExercise> strengthExercises
    ) {
        RunningDetailsResponse runningResponse =
                runningDetails == null
                        ? null
                        : RunningDetailsResponse.from(runningDetails);

        List<BoulderingGradeResultResponse> boulderingResponses =
                boulderingResults == null
                        ? List.of()
                        : boulderingResults.stream()
                                .map(BoulderingGradeResultResponse::from)
                                .toList();

        List<StrengthExerciseResponse> strengthResponses =
                strengthExercises == null
                        ? List.of()
                        : strengthExercises.stream()
                                .map(StrengthExerciseResponse::from)
                                .toList();

        return new TrainingSessionResponse(
                session.getId(),
                session.getType(),
                session.getTrainingDate(),
                session.getDurationMinutes(),
                session.getNotes(),
                runningResponse,
                boulderingResponses,
                strengthResponses
        );
    }
}