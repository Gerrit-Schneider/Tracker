package com.peakprogress.backend.training.gym;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StrengthSetRequest(

        @Min(1)
        int repetitions,

        @NotNull
        @DecimalMin("0.0")
        @Digits(integer = 4, fraction = 2)
        BigDecimal weightKg
) {
}