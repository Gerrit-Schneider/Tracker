package com.peakprogress.backend.training.csv;

import com.peakprogress.backend.training.TrainingSessionResponse;
import com.peakprogress.backend.training.TrainingSessionService;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Service
public class TrainingSessionCsvExportService {

    private static final int COLUMN_COUNT = 20;

    private static final String[] HEADERS = {
            "session_key",
            "type",
            "training_date",
            "duration_minutes",
            "notes",
            "detail_type",
            "run_type",
            "distance_meters",
            "elapsed_seconds",
            "pace_seconds_per_kilometer",
            "average_heart_rate",
            "max_heart_rate",
            "bouldering_grade",
            "attempted_count",
            "completed_count",
            "exercise_name",
            "exercise_order",
            "set_number",
            "repetitions",
            "weight_kg"
    };

    private final TrainingSessionService trainingSessionService;

    public TrainingSessionCsvExportService(
            TrainingSessionService trainingSessionService
    ) {
        this.trainingSessionService = trainingSessionService;
    }

    public byte[] export() {
        List<TrainingSessionResponse> sessions =
                trainingSessionService.findAll(null);

        StringWriter writer = new StringWriter();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader(HEADERS)
                .setRecordSeparator("\r\n")
                .get();

        try (CSVPrinter printer =
                     new CSVPrinter(writer, format)) {

            for (TrainingSessionResponse session : sessions) {
                printSession(printer, session);
            }
        } catch (IOException exception) {
            throw new IllegalStateException(
                    "CSV-Export konnte nicht erstellt werden.",
                    exception
            );
        }

        String csvWithBom = "\uFEFF" + writer;

        return csvWithBom.getBytes(StandardCharsets.UTF_8);
    }

    private void printSession(
            CSVPrinter printer,
            TrainingSessionResponse session
    ) throws IOException {
        boolean detailWritten = false;

        if (session.runningDetails() != null) {
            String[] row = createBaseRow(session, "RUNNING");

            set(row, 6, session.runningDetails().runType());
            set(row, 7, session.runningDetails().distanceMeters());
            set(row, 8, session.runningDetails().elapsedSeconds());
            set(
                    row,
                    9,
                    session.runningDetails()
                            .paceSecondsPerKilometer()
            );
            set(
                    row,
                    10,
                    session.runningDetails().averageHeartRate()
            );
            set(
                    row,
                    11,
                    session.runningDetails().maxHeartRate()
            );

            printer.printRecord(Arrays.asList(row));
            detailWritten = true;
        }

        for (var result : session.boulderingResults()) {
            String[] row = createBaseRow(
                    session,
                    "BOULDERING"
            );

            set(row, 12, result.grade());
            set(row, 13, result.attemptedCount());
            set(row, 14, result.completedCount());

            printer.printRecord(Arrays.asList(row));
            detailWritten = true;
        }

        for (var exercise : session.strengthExercises()) {
            for (var strengthSet : exercise.sets()) {
                String[] row = createBaseRow(
                        session,
                        "STRENGTH_SET"
                );

                set(row, 15, exercise.exerciseName());
                set(row, 16, exercise.exerciseOrder());
                set(row, 17, strengthSet.setNumber());
                set(row, 18, strengthSet.repetitions());
                set(row, 19, strengthSet.weightKg());

                printer.printRecord(Arrays.asList(row));
                detailWritten = true;
            }
        }

        if (!detailWritten) {
            printer.printRecord(
                    Arrays.asList(
                            createBaseRow(session, "NONE")
                    )
            );
        }
    }

    private String[] createBaseRow(
            TrainingSessionResponse session,
            String detailType
    ) {
        String[] row = new String[COLUMN_COUNT];
        Arrays.fill(row, "");

        set(row, 0, session.id());
        set(row, 1, session.type());
        set(row, 2, session.trainingDate());
        set(row, 3, session.durationMinutes());
        set(row, 4, session.notes());
        set(row, 5, detailType);

        return row;
    }

    private void set(
            String[] row,
            int index,
            Object value
    ) {
        if (value != null) {
            row[index] = value.toString();
        }
    }
}