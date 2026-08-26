package com.peakprogress.backend.training.bouldering;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record BoulderingGradeResultRequest(

        @NotNull
        BoulderingGrade grade,

        @Min(0)
        int attemptedCount,

        @Min(0)
        int completedCount
) {

    @AssertTrue(
            message = "Die Anzahl geschaffter Boulder darf "
                    + "nicht größer als die Anzahl der Versuche sein."
    )
    public boolean isCompletedCountValid() {
        return completedCount <= attemptedCount;
    }
}