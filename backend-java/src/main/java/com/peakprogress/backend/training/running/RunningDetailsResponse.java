package com.peakprogress.backend.training.running;

public record RunningDetailsResponse(
        RunType runType,
        int distanceMeters,
        int elapsedSeconds,
        int paceSecondsPerKilometer,
        Integer averageHeartRate,
        Integer maxHeartRate
) {

    public static RunningDetailsResponse from(
            RunningDetails details
    ) {
        return new RunningDetailsResponse(
                details.getRunType(),
                details.getDistanceMeters(),
                details.getElapsedSeconds(),
                details.getPaceSecondsPerKilometer(),
                details.getAverageHeartRate(),
                details.getMaxHeartRate()
        );
    }
}