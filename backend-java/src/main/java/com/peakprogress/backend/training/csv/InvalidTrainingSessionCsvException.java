package com.peakprogress.backend.training.csv;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class InvalidTrainingSessionCsvException
        extends RuntimeException {

    public InvalidTrainingSessionCsvException(String message) {
        super(message);
    }

    public InvalidTrainingSessionCsvException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}