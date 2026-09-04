package com.peakprogress.backend.goal;

import java.math.BigDecimal;

public record TrainingGoalProgress(
        BigDecimal currentValue,
        BigDecimal progressPercent,
        boolean completed
) {

    public static TrainingGoalProgress noData() {
        return new TrainingGoalProgress(
                null,
                BigDecimal.ZERO,
                false
        );
    }
}