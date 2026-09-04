package com.peakprogress.backend.training.csv;

import com.peakprogress.backend.training.CreateTrainingSessionRequest;
import com.peakprogress.backend.training.TrainingType;
import com.peakprogress.backend.training.bouldering.BoulderingGrade;
import com.peakprogress.backend.training.bouldering.BoulderingGradeResultRequest;
import com.peakprogress.backend.training.gym.StrengthExerciseRequest;
import com.peakprogress.backend.training.gym.StrengthSetRequest;
import com.peakprogress.backend.training.running.RunningDetailsRequest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

final class CsvSessionAccumulator {

    private final String sessionKey;
    private final TrainingType type;
    private final LocalDate trainingDate;
    private final int durationMinutes;
    private final String notes;

    private RunningDetailsRequest runningDetails;

    private final Map<
            BoulderingGrade,
            BoulderingGradeResultRequest
            > boulderingResults = new LinkedHashMap<>();

    private final Map<
            Integer,
            StrengthExerciseAccumulator
            > strengthExercises = new TreeMap<>();

    private boolean hasDetails;
    private boolean noDetailsRow;

    CsvSessionAccumulator(
            String sessionKey,
            TrainingType type,
            LocalDate trainingDate,
            int durationMinutes,
            String notes
    ) {
        this.sessionKey = sessionKey;
        this.type = type;
        this.trainingDate = trainingDate;
        this.durationMinutes = durationMinutes;
        this.notes = notes;
    }

    void verifyBaseValues(
            TrainingType rowType,
            LocalDate rowDate,
            int rowDuration,
            String rowNotes,
            long rowNumber
    ) {
        boolean valuesMatch =
                type == rowType
                        && trainingDate.equals(rowDate)
                        && durationMinutes == rowDuration
                        && Objects.equals(notes, rowNotes);

        if (!valuesMatch) {
            throw invalid(
                    rowNumber,
                    "Die Grunddaten für session_key "
                            + sessionKey
                            + " sind nicht in allen Zeilen gleich."
            );
        }
    }

    void addRunningDetails(
            RunningDetailsRequest details,
            long rowNumber
    ) {
        requireType(
                TrainingType.RUNNING,
                rowNumber,
                "RUNNING"
        );

        ensureDetailCanBeAdded(rowNumber);

        if (runningDetails != null) {
            throw invalid(
                    rowNumber,
                    "Für session_key "
                            + sessionKey
                            + " existieren mehrere Laufdetail-Zeilen."
            );
        }

        runningDetails = details;
        hasDetails = true;
    }

    void addBoulderingResult(
            BoulderingGradeResultRequest result,
            long rowNumber
    ) {
        requireType(
                TrainingType.BOULDERING,
                rowNumber,
                "BOULDERING"
        );

        ensureDetailCanBeAdded(rowNumber);

        if (boulderingResults.putIfAbsent(
                result.grade(),
                result
        ) != null) {
            throw invalid(
                    rowNumber,
                    "Der Bouldering-Grad "
                            + result.grade()
                            + " kommt für session_key "
                            + sessionKey
                            + " mehrfach vor."
            );
        }

        hasDetails = true;
    }

    void addStrengthSet(
            int exerciseOrder,
            String exerciseName,
            int setNumber,
            StrengthSetRequest strengthSet,
            long rowNumber
    ) {
        requireType(
                TrainingType.STRENGTH,
                rowNumber,
                "STRENGTH_SET"
        );

        ensureDetailCanBeAdded(rowNumber);

        StrengthExerciseAccumulator exercise =
                strengthExercises.computeIfAbsent(
                        exerciseOrder,
                        ignored ->
                                new StrengthExerciseAccumulator(
                                        exerciseName
                                )
                );

        exercise.addSet(
                exerciseName,
                setNumber,
                strengthSet,
                sessionKey,
                rowNumber
        );

        hasDetails = true;
    }

    void markNoDetails(long rowNumber) {
        if (hasDetails || noDetailsRow) {
            throw invalid(
                    rowNumber,
                    "Die NONE-Zeile für session_key "
                            + sessionKey
                            + " ist doppelt oder widersprüchlich."
            );
        }

        noDetailsRow = true;
    }

    CreateTrainingSessionRequest toRequest() {
        List<StrengthExerciseRequest> exerciseRequests =
                strengthExercises.values()
                        .stream()
                        .map(
                                StrengthExerciseAccumulator
                                        ::toRequest
                        )
                        .toList();

        return new CreateTrainingSessionRequest(
                type,
                trainingDate,
                durationMinutes,
                notes,
                runningDetails,
                new ArrayList<>(boulderingResults.values()),
                exerciseRequests
        );
    }

    private void ensureDetailCanBeAdded(long rowNumber) {
        if (noDetailsRow) {
            throw invalid(
                    rowNumber,
                    "Für session_key "
                            + sessionKey
                            + " wurden NONE und Detailzeilen gemischt."
            );
        }
    }

    private void requireType(
            TrainingType expectedType,
            long rowNumber,
            String detailType
    ) {
        if (type != expectedType) {
            throw invalid(
                    rowNumber,
                    detailType
                            + " passt nicht zur Trainingsart "
                            + type
                            + "."
            );
        }
    }

    private InvalidTrainingSessionCsvException invalid(
            long rowNumber,
            String message
    ) {
        return new InvalidTrainingSessionCsvException(
                "CSV-Zeile " + rowNumber + ": " + message
        );
    }

    private static final class StrengthExerciseAccumulator {

        private final String exerciseName;

        private final Map<Integer, StrengthSetRequest> sets =
                new TreeMap<>();

        private StrengthExerciseAccumulator(
                String exerciseName
        ) {
            this.exerciseName = exerciseName;
        }

        private void addSet(
                String rowExerciseName,
                int setNumber,
                StrengthSetRequest strengthSet,
                String sessionKey,
                long rowNumber
        ) {
            if (!exerciseName.equals(rowExerciseName)) {
                throw new InvalidTrainingSessionCsvException(
                        "CSV-Zeile "
                                + rowNumber
                                + ": Für session_key "
                                + sessionKey
                                + " besitzt dieselbe exercise_order "
                                + "unterschiedliche Übungsnamen."
                );
            }

            if (sets.putIfAbsent(
                    setNumber,
                    strengthSet
            ) != null) {
                throw new InvalidTrainingSessionCsvException(
                        "CSV-Zeile "
                                + rowNumber
                                + ": Satz "
                                + setNumber
                                + " der Übung "
                                + exerciseName
                                + " kommt mehrfach vor."
                );
            }
        }

        private StrengthExerciseRequest toRequest() {
            return new StrengthExerciseRequest(
                    exerciseName,
                    List.copyOf(sets.values())
            );
        }
    }
}