package com.peakprogress.backend.goal;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TrainingGoalNotFoundException extends RuntimeException {

    public TrainingGoalNotFoundException(Long id) {
        super("Trainingsziel mit ID " + id + " wurde nicht gefunden.");
    }
}