package com.peakprogress.backend.training;

import org.springframework.data.domain.Page;

import java.util.List;

public record TrainingSessionPageResponse(
        List<TrainingSessionResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {

    public static TrainingSessionPageResponse from(
            Page<TrainingSessionResponse> result
    ) {
        return new TrainingSessionPageResponse(
                result.getContent(),
                result.getNumber(),
                result.getSize(),
                result.getTotalElements(),
                result.getTotalPages(),
                result.isFirst(),
                result.isLast()
        );
    }
}