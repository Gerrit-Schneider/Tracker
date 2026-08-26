package com.peakprogress.backend.training.gym;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;

public record StrengthExerciseRequest(

        @NotBlank
        @Size(max = 100)
        String exerciseName,

        @NotEmpty
        @Size(max = 50)
        List<@Valid StrengthSetRequest> sets
) {
}