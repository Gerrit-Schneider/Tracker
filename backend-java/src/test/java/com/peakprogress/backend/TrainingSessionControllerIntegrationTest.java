package com.peakprogress.backend;

import com.peakprogress.backend.training.TrainingSessionRepository;
import com.peakprogress.backend.training.bouldering.BoulderingGradeResultRepository;
import com.peakprogress.backend.training.running.RunningDetailsRepository;
import com.peakprogress.backend.training.gym.StrengthExercise;
import com.peakprogress.backend.training.gym.StrengthExerciseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainingDetailsControllerIntegrationTest {

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
    void createsRunningDetailsAndCalculatesPace() throws Exception {
        String requestBody = """
                {
                    "type": "RUNNING",
                    "trainingDate": "2026-08-20",
                    "durationMinutes": 26,
                    "notes": "Lockerer Testlauf",
                    "runningDetails": {
                        "runType": "EASY",
                        "distanceMeters": 5000,
                        "elapsedSeconds": 1560,
                        "averageHeartRate": 148,
                        "maxHeartRate": 169
                    }
                }
                """;

        mockMvc.perform(post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.runningDetails.runType")
                        .value("EASY"))
                .andExpect(jsonPath("$.runningDetails.distanceMeters")
                        .value(5000))
                .andExpect(jsonPath(
                        "$.runningDetails.paceSecondsPerKilometer"
                ).value(312))
                .andExpect(jsonPath(
                        "$.runningDetails.averageHeartRate"
                ).value(148))
                .andExpect(jsonPath(
                        "$.runningDetails.maxHeartRate"
                ).value(169));

        assertEquals(1, runningRepository.count());
    }

    @Test
    void createsBoulderingGradeResults() throws Exception {
        String requestBody = """
                {
                    "type": "BOULDERING",
                    "trainingDate": "2026-08-20",
                    "durationMinutes": 90,
                    "notes": "Bouldering-Test",
                    "boulderingResults": [
                        {
                            "grade": "V4",
                            "attemptedCount": 6,
                            "completedCount": 3
                        },
                        {
                            "grade": "V5",
                            "attemptedCount": 4,
                            "completedCount": 1
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(
                        "$.boulderingResults.length()"
                ).value(2))
                .andExpect(jsonPath(
                        "$.boulderingResults[0].grade"
                ).value("V4"))
                .andExpect(jsonPath(
                        "$.boulderingResults[0].completedCount"
                ).value(3))
                .andExpect(jsonPath(
                        "$.boulderingResults[1].grade"
                ).value("V5"));

        assertEquals(2, boulderingRepository.count());
    }

    @Test
    void createsStrengthExercisesAndSets() throws Exception {
        String requestBody = """
                {
                    "type": "STRENGTH",
                    "trainingDate": "2026-08-20",
                    "durationMinutes": 75,
                    "notes": "Krafttraining-Test",
                    "strengthExercises": [
                        {
                            "exerciseName": "Kreuzheben",
                            "sets": [
                                {
                                    "repetitions": 5,
                                    "weightKg": 120
                                },
                                {
                                    "repetitions": 5,
                                    "weightKg": 120
                                },
                                {
                                    "repetitions": 4,
                                    "weightKg": 120
                                }
                            ]
                        }
                    ]
                }
                """;

        mockMvc.perform(post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath(
                        "$.strengthExercises.length()"
                ).value(1))
                .andExpect(jsonPath(
                        "$.strengthExercises[0].exerciseName"
                ).value("Kreuzheben"))
                .andExpect(jsonPath(
                        "$.strengthExercises[0].sets.length()"
                ).value(3))
                .andExpect(jsonPath(
                        "$.strengthExercises[0].sets[0].repetitions"
                ).value(5))
                .andExpect(jsonPath(
                        "$.strengthExercises[0].volumeKg"
                ).value(1680.0));

        List<StrengthExercise> exercises =
                strengthRepository
                        .findAllBySession_IdOrderByExerciseOrderAsc(
                                sessionRepository.findAll().get(0).getId()
                        );

        assertEquals(1, exercises.size());
        assertEquals(3, exercises.get(0).getSets().size());
    }

    @Test
    void rejectsDetailsForWrongTrainingType() throws Exception {
        String requestBody = """
                {
                    "type": "BOULDERING",
                    "trainingDate": "2026-08-20",
                    "durationMinutes": 60,
                    "runningDetails": {
                        "runType": "EASY",
                        "distanceMeters": 5000,
                        "elapsedSeconds": 1500
                    }
                }
                """;

        mockMvc.perform(post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        assertEquals(0, sessionRepository.count());
    }
}