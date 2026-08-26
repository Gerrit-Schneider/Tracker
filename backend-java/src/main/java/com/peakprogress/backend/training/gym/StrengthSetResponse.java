package com.peakprogress.backend.training.gym;

import java.math.BigDecimal;

public record StrengthSetResponse(
        Long id,
        int setNumber,
        int repetitions,
        BigDecimal weightKg,
        BigDecimal volumeKg
) {

    public static StrengthSetResponse from(StrengthSet strengthSet) {
        return new StrengthSetResponse(
                strengthSet.getId(),
                strengthSet.getSetNumber(),
                strengthSet.getRepetitions(),
                strengthSet.getWeightKg(),
                strengthSet.getVolumeKg()
        );
    }
}