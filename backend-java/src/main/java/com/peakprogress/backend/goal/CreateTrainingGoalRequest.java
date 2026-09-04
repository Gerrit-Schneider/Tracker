package com.peakprogress.backend.goal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateTrainingGoalRequest(

        @NotBlank
        @Size(max = 120)
        String title,

        @NotNull
        GoalMetric metric,

        @NotNull
        @DecimalMin("0.01")
        @Digits(integer = 10, fraction = 2)
        BigDecimal targetValue,

        @Size(max = 120)
        String exerciseName,

        @FutureOrPresent
        LocalDate targetDate
) {
}