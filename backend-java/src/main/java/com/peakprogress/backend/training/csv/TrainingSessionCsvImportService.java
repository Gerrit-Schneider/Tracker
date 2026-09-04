package com.peakprogress.backend.training.csv;

import com.peakprogress.backend.training.CreateTrainingSessionRequest;
import com.peakprogress.backend.training.TrainingSessionService;
import com.peakprogress.backend.training.TrainingType;
import com.peakprogress.backend.training.bouldering.BoulderingGrade;
import com.peakprogress.backend.training.bouldering.BoulderingGradeResultRequest;
import com.peakprogress.backend.training.gym.StrengthSetRequest;
import com.peakprogress.backend.training.running.RunType;
import com.peakprogress.backend.training.running.RunningDetailsRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class TrainingSessionCsvImportService {

    private static final long MAXIMUM_FILE_SIZE =
            5L * 1024L * 1024L;

    private static final List<String> REQUIRED_HEADERS = List.of(
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
    );

    private final TrainingSessionService trainingSessionService;
    private final Validator validator;

    public TrainingSessionCsvImportService(
            TrainingSessionService trainingSessionService,
            Validator validator
    ) {
        this.trainingSessionService = trainingSessionService;
        this.validator = validator;
    }

    @Transactional
    public TrainingSessionCsvImportResponse importCsv(
            MultipartFile file
    ) {
        validateFile(file);

        String content = readFile(file);
        Map<String, CsvSessionAccumulator> sessions =
                new LinkedHashMap<>();

        long processedRows = parseCsv(content, sessions);

        if (sessions.isEmpty()) {
            throw new InvalidTrainingSessionCsvException(
                    "Die CSV-Datei enthält keine Trainingseinheiten."
            );
        }

        for (Map.Entry<String, CsvSessionAccumulator> entry
                : sessions.entrySet()) {

            CreateTrainingSessionRequest request =
                    entry.getValue().toRequest();

            validateRequest(entry.getKey(), request);
            trainingSessionService.create(request);
        }

        return new TrainingSessionCsvImportResponse(
                sessions.size(),
                processedRows
        );
    }

    private long parseCsv(
            String content,
            Map<String, CsvSessionAccumulator> sessions
    ) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setDelimiter(';')
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreEmptyLines(true)
                .get();

        long processedRows = 0;

        try (CSVParser parser = CSVParser.parse(content, format)) {
            validateHeaders(parser.getHeaderMap());

            for (CSVRecord record : parser) {
                parseRecord(record, sessions);
                processedRows++;
            }
        } catch (InvalidTrainingSessionCsvException exception) {
            throw exception;
        } catch (IOException exception) {
            throw new InvalidTrainingSessionCsvException(
                    "Die CSV-Datei konnte nicht gelesen werden.",
                    exception
            );
        }

        return processedRows;
    }

    private void parseRecord(
            CSVRecord record,
            Map<String, CsvSessionAccumulator> sessions
    ) {
        long rowNumber = record.getRecordNumber() + 1;

        String sessionKey = required(
                record,
                "session_key",
                rowNumber
        );

        TrainingType type = parseEnum(
                record,
                "type",
                TrainingType.class,
                rowNumber
        );

        LocalDate trainingDate = parseDate(
                record,
                "training_date",
                rowNumber
        );

        int durationMinutes = parseInteger(
                record,
                "duration_minutes",
                rowNumber
        );

        String notes = optionalText(record, "notes");

        CsvSessionAccumulator accumulator =
                sessions.get(sessionKey);

        if (accumulator == null) {
            accumulator = new CsvSessionAccumulator(
                    sessionKey,
                    type,
                    trainingDate,
                    durationMinutes,
                    notes
            );

            sessions.put(sessionKey, accumulator);
        } else {
            accumulator.verifyBaseValues(
                    type,
                    trainingDate,
                    durationMinutes,
                    notes,
                    rowNumber
            );
        }

        String detailType = required(
                record,
                "detail_type",
                rowNumber
        ).toUpperCase(Locale.ROOT);

        switch (detailType) {
            case "NONE" ->
                    accumulator.markNoDetails(rowNumber);

            case "RUNNING" ->
                    parseRunningDetails(
                            record,
                            accumulator,
                            rowNumber
                    );

            case "BOULDERING" ->
                    parseBoulderingResult(
                            record,
                            accumulator,
                            rowNumber
                    );

            case "STRENGTH_SET" ->
                    parseStrengthSet(
                            record,
                            accumulator,
                            rowNumber
                    );

            default ->
                    throw invalid(
                            rowNumber,
                            "Unbekannter detail_type: "
                                    + detailType
                    );
        }
    }

    private void parseRunningDetails(
            CSVRecord record,
            CsvSessionAccumulator accumulator,
            long rowNumber
    ) {
        RunningDetailsRequest details =
                new RunningDetailsRequest(
                        parseEnum(
                                record,
                                "run_type",
                                RunType.class,
                                rowNumber
                        ),
                        parseInteger(
                                record,
                                "distance_meters",
                                rowNumber
                        ),
                        parseInteger(
                                record,
                                "elapsed_seconds",
                                rowNumber
                        ),
                        parseOptionalInteger(
                                record,
                                "average_heart_rate",
                                rowNumber
                        ),
                        parseOptionalInteger(
                                record,
                                "max_heart_rate",
                                rowNumber
                        )
                );

        accumulator.addRunningDetails(details, rowNumber);
    }

    private void parseBoulderingResult(
            CSVRecord record,
            CsvSessionAccumulator accumulator,
            long rowNumber
    ) {
        BoulderingGradeResultRequest result =
                new BoulderingGradeResultRequest(
                        parseEnum(
                                record,
                                "bouldering_grade",
                                BoulderingGrade.class,
                                rowNumber
                        ),
                        parseInteger(
                                record,
                                "attempted_count",
                                rowNumber
                        ),
                        parseInteger(
                                record,
                                "completed_count",
                                rowNumber
                        )
                );

        accumulator.addBoulderingResult(result, rowNumber);
    }

    private void parseStrengthSet(
            CSVRecord record,
            CsvSessionAccumulator accumulator,
            long rowNumber
    ) {
        String exerciseName = required(
                record,
                "exercise_name",
                rowNumber
        );

        int exerciseOrder = parsePositiveInteger(
                record,
                "exercise_order",
                rowNumber
        );

        int setNumber = parsePositiveInteger(
                record,
                "set_number",
                rowNumber
        );

        StrengthSetRequest strengthSet =
                new StrengthSetRequest(
                        parseInteger(
                                record,
                                "repetitions",
                                rowNumber
                        ),
                        parseDecimal(
                                record,
                                "weight_kg",
                                rowNumber
                        )
                );

        accumulator.addStrengthSet(
                exerciseOrder,
                exerciseName,
                setNumber,
                strengthSet,
                rowNumber
        );
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidTrainingSessionCsvException(
                    "Bitte wähle eine nicht leere CSV-Datei aus."
            );
        }

        if (file.getSize() > MAXIMUM_FILE_SIZE) {
            throw new InvalidTrainingSessionCsvException(
                    "Die CSV-Datei darf maximal 5 MB groß sein."
            );
        }

        String filename = file.getOriginalFilename();

        if (filename != null
                && !filename.toLowerCase(Locale.ROOT)
                        .endsWith(".csv")) {
            throw new InvalidTrainingSessionCsvException(
                    "Es sind nur Dateien mit der Endung .csv erlaubt."
            );
        }
    }

    private String readFile(MultipartFile file) {
        try {
            String content = new String(
                    file.getBytes(),
                    StandardCharsets.UTF_8
            );

            if (content.startsWith("\uFEFF")) {
                return content.substring(1);
            }

            return content;
        } catch (IOException exception) {
            throw new InvalidTrainingSessionCsvException(
                    "Die CSV-Datei konnte nicht gelesen werden.",
                    exception
            );
        }
    }

    private void validateHeaders(
            Map<String, Integer> headers
    ) {
        List<String> missingHeaders = REQUIRED_HEADERS.stream()
                .filter(header -> !headers.containsKey(header))
                .toList();

        if (!missingHeaders.isEmpty()) {
            throw new InvalidTrainingSessionCsvException(
                    "In der CSV-Datei fehlen Spalten: "
                            + String.join(", ", missingHeaders)
            );
        }
    }

    private void validateRequest(
            String sessionKey,
            CreateTrainingSessionRequest request
    ) {
        Set<ConstraintViolation<CreateTrainingSessionRequest>>
                violations = validator.validate(request);

        if (violations.isEmpty()) {
            return;
        }

        String validationErrors = violations.stream()
                .map(violation ->
                        violation.getPropertyPath()
                                + ": "
                                + violation.getMessage()
                )
                .sorted()
                .collect(Collectors.joining(", "));

        throw new InvalidTrainingSessionCsvException(
                "Ungültige Trainingseinheit "
                        + sessionKey
                        + ": "
                        + validationErrors
        );
    }

    private String required(
            CSVRecord record,
            String column,
            long rowNumber
    ) {
        String value = record.get(column).trim();

        if (value.isEmpty()) {
            throw invalid(
                    rowNumber,
                    "Die Spalte " + column + " darf nicht leer sein."
            );
        }

        return value;
    }

    private String optionalText(
            CSVRecord record,
            String column
    ) {
        String value = record.get(column);

        return value.isBlank() ? null : value;
    }

    private int parseInteger(
            CSVRecord record,
            String column,
            long rowNumber
    ) {
        String value = required(record, column, rowNumber);

        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException exception) {
            throw invalid(
                    rowNumber,
                    column + " muss eine ganze Zahl sein."
            );
        }
    }

    private int parsePositiveInteger(
            CSVRecord record,
            String column,
            long rowNumber
    ) {
        int value = parseInteger(record, column, rowNumber);

        if (value < 1) {
            throw invalid(
                    rowNumber,
                    column + " muss mindestens 1 sein."
            );
        }

        return value;
    }

    private Integer parseOptionalInteger(
            CSVRecord record,
            String column,
            long rowNumber
    ) {
        String value = record.get(column).trim();

        if (value.isEmpty()) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException exception) {
            throw invalid(
                    rowNumber,
                    column + " muss eine ganze Zahl sein."
            );
        }
    }

    private BigDecimal parseDecimal(
            CSVRecord record,
            String column,
            long rowNumber
    ) {
        String value = required(record, column, rowNumber);

        try {
            return new BigDecimal(value);
        } catch (NumberFormatException exception) {
            throw invalid(
                    rowNumber,
                    column + " muss eine Dezimalzahl sein."
            );
        }
    }

    private LocalDate parseDate(
            CSVRecord record,
            String column,
            long rowNumber
    ) {
        String value = required(record, column, rowNumber);

        try {
            return LocalDate.parse(value);
        } catch (DateTimeParseException exception) {
            throw invalid(
                    rowNumber,
                    column
                            + " muss das Format YYYY-MM-DD besitzen."
            );
        }
    }

    private <T extends Enum<T>> T parseEnum(
            CSVRecord record,
            String column,
            Class<T> enumType,
            long rowNumber
    ) {
        String value = required(
                record,
                column,
                rowNumber
        ).toUpperCase(Locale.ROOT);

        try {
            return Enum.valueOf(enumType, value);
        } catch (IllegalArgumentException exception) {
            throw invalid(
                    rowNumber,
                    "Ungültiger Wert für "
                            + column
                            + ": "
                            + value
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
}