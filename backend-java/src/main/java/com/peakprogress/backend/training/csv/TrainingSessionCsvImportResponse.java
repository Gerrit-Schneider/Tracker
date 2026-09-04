package com.peakprogress.backend.training.csv;

public record TrainingSessionCsvImportResponse(
        int importedSessions,
        long processedRows
) {
}