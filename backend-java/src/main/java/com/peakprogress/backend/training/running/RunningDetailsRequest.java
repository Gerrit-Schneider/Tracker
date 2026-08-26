package com.peakprogress.backend.training.running;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record RunningDetailsRequest(

        @NotNull
        RunType runType,

        @Min(1)
        int distanceMeters,

        @Min(1)
        int elapsedSeconds,

        @Min(30)
        @Max(250)
        Integer averageHeartRate,

        @Min(30)
        @Max(250)
        Integer maxHeartRate
) {

    @AssertTrue(
            message = "Die maximale Herzfrequenz muss mindestens "
                    + "der durchschnittlichen Herzfrequenz entsprechen."
    )
    public boolean isHeartRateOrderValid() {
        return averageHeartRate == null
                || maxHeartRate == null
                || maxHeartRate >= averageHeartRate;
    }
}