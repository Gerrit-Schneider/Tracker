package com.peakprogress.backend.training;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class TrainingSessionNotFoundException
        extends RuntimeException {

    public TrainingSessionNotFoundException(Long id) {
        super("Trainingseinheit mit ID " + id + " wurde nicht gefunden.");
    }
}