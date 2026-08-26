package com.peakprogress.backend.training.bouldering;

public record BoulderingGradeResultResponse(
        Long id,
        BoulderingGrade grade,
        int attemptedCount,
        int completedCount
) {

    public static BoulderingGradeResultResponse from(
            BoulderingGradeResult result
    ) {
        return new BoulderingGradeResultResponse(
                result.getId(),
                result.getGrade(),
                result.getAttemptedCount(),
                result.getCompletedCount()
        );
    }
}