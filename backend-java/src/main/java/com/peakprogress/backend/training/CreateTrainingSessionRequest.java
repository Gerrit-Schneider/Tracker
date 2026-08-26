package com.peakprogress.backend.training;

import com.peakprogress.backend.training.bouldering.BoulderingGradeResultRequest;
import com.peakprogress.backend.training.running.RunningDetailsRequest;
import com.peakprogress.backend.training.gym.StrengthExerciseRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record CreateTrainingSessionRequest(

        @NotNull
        TrainingType type,

        @NotNull
        @PastOrPresent
        LocalDate trainingDate,

        @Min(1)
        @Max(1440)
        int durationMinutes,

        @Size(max = 1000)
        String notes,

        @Valid
        RunningDetailsRequest runningDetails,

        @Size(max = 19)
        List<@Valid BoulderingGradeResultRequest> boulderingResults,

        @Size(max = 50)
        List<@Valid StrengthExerciseRequest> strengthExercises
) {

    public CreateTrainingSessionRequest(
            TrainingType type,
            LocalDate trainingDate,
            int durationMinutes,
            String notes
    ) {
        this(
                type,
                trainingDate,
                durationMinutes,
                notes,
                null,
                null,
                null
        );
    }
}