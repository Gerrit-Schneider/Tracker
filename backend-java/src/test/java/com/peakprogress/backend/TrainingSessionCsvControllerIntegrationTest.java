package com.peakprogress.backend;

import com.peakprogress.backend.training.TrainingSessionRepository;
import com.peakprogress.backend.training.bouldering.BoulderingGradeResultRepository;
import com.peakprogress.backend.training.gym.StrengthExerciseRepository;
import com.peakprogress.backend.training.running.RunningDetailsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(PostgresTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class TrainingSessionCsvControllerIntegrationTest {

    private static final String HEADER = String.join(
            ";",
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

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainingSessionRepository sessionRepository;

    @Autowired
    private RunningDetailsRepository runningRepository;

    @Autowired
    private BoulderingGradeResultRepository boulderingRepository;

    @Autowired
    private StrengthExerciseRepository strengthRepository;

    @BeforeEach
    void clearDatabase() {
        sessionRepository.deleteAll();
    }

    @Test
    void importsAllTrainingTypes() throws Exception {
        String csv = createCsv(
                runningRow("run-1", "Testlauf"),
                boulderingRow("boulder-1"),
                strengthRow("strength-1")
        );

        MockMultipartFile file = csvFile(csv);

        mockMvc.perform(
                        multipart(
                                "/api/training-sessions/import.csv"
                        ).file(file)
                )
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.importedSessions").value(3)
                )
                .andExpect(
                        jsonPath("$.processedRows").value(3)
                );

        assertEquals(3, sessionRepository.count());
        assertEquals(1, runningRepository.count());
        assertEquals(1, boulderingRepository.count());
        assertEquals(1, strengthRepository.count());
    }

    @Test
    void exportsImportedTrainingSession() throws Exception {
        MockMultipartFile file = csvFile(
                createCsv(
                        runningRow("run-export", "Exporttest")
                )
        );

        mockMvc.perform(
                        multipart(
                                "/api/training-sessions/import.csv"
                        ).file(file)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get(
                                "/api/training-sessions/export.csv"
                        )
                )
                .andExpect(status().isOk())
                .andExpect(
                        header().string(
                                HttpHeaders.CONTENT_DISPOSITION,
                                containsString(
                                        "peak-progress-training.csv"
                                )
                        )
                )
                .andExpect(
                        content().contentTypeCompatibleWith(
                                "text/csv"
                        )
                )
                .andExpect(
                        content().string(
                                containsString(
                                        "session_key;type;"
                                )
                        )
                )
                .andExpect(
                        content().string(
                                containsString(
                                        "RUNNING;2026-09-01;30;"
                                                + "Exporttest;RUNNING;"
                                                + "EASY;5000;1800"
                                )
                        )
                );
    }

    @Test
    void rollsBackCompleteImportWhenOneSessionIsInvalid()
            throws Exception {

        String csv = createCsv(
                noDetailsRow("valid-session", "30"),
                noDetailsRow("invalid-session", "0")
        );

        mockMvc.perform(
                        multipart(
                                "/api/training-sessions/import.csv"
                        ).file(csvFile(csv))
                )
                .andExpect(status().isBadRequest());

        assertEquals(0, sessionRepository.count());
    }

    private static MockMultipartFile csvFile(String csv) {
        return new MockMultipartFile(
                "file",
                "training.csv",
                "text/csv",
                csv.getBytes(StandardCharsets.UTF_8)
        );
    }

    private static String createCsv(String... rows) {
        return HEADER
                + "\r\n"
                + String.join("\r\n", rows)
                + "\r\n";
    }

    private static String runningRow(
            String sessionKey,
            String notes
    ) {
        return row(
                sessionKey,
                "RUNNING",
                "2026-09-01",
                "30",
                notes,
                "RUNNING",
                "EASY",
                "5000",
                "1800",
                "360",
                "145",
                "165",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                ""
        );
    }

    private static String boulderingRow(
            String sessionKey
    ) {
        return row(
                sessionKey,
                "BOULDERING",
                "2026-09-02",
                "90",
                "Bouldertest",
                "BOULDERING",
                "",
                "",
                "",
                "",
                "",
                "",
                "V5",
                "4",
                "1",
                "",
                "",
                "",
                "",
                ""
        );
    }

    private static String strengthRow(
            String sessionKey
    ) {
        return row(
                sessionKey,
                "STRENGTH",
                "2026-09-03",
                "60",
                "Krafttest",
                "STRENGTH_SET",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "Klimmzug",
                "1",
                "1",
                "2",
                "45.00"
        );
    }

    private static String noDetailsRow(
            String sessionKey,
            String durationMinutes
    ) {
        String[] columns = new String[20];
        Arrays.fill(columns, "");

        columns[0] = sessionKey;
        columns[1] = "RUNNING";
        columns[2] = "2026-09-01";
        columns[3] = durationMinutes;
        columns[5] = "NONE";

        return String.join(";", columns);
    }

    private static String row(String... columns) {
        return String.join(";", columns);
    }
}