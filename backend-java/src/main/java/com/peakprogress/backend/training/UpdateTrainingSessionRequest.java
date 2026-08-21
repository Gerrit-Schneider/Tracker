package com.peakprogress.backend.training;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UpdateTrainingSessionRequest(

        @NotNull
        TrainingType type,

        @NotNull
        @PastOrPresent
        LocalDate trainingDate,

        @Min(1)
        @Max(1440)
        int durationMinutes,

        @Size(max = 1000)
        String notes
) {
}