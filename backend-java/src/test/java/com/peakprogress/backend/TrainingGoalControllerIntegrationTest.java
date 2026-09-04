package com.peakprogress.backend;

import com.peakprogress.backend.goal.TrainingGoalRepository;
import com.peakprogress.backend.training.TrainingSessionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(PostgresTestConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class TrainingGoalControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainingGoalRepository goalRepository;

    @Autowired
    private TrainingSessionRepository sessionRepository;

    @BeforeEach
    void clearDatabase() {
        goalRepository.deleteAll();
        sessionRepository.deleteAll();
    }

    @Test
    void createsAndListsTrainingGoal() throws Exception {
        String requestBody = """
                {
                    "title": "12 km in einem Lauf",
                    "metric": "RUNNING_DISTANCE_KM",
                    "targetValue": 12,
                    "targetDate": "2035-12-31"
                }
                """;

        mockMvc.perform(post("/api/training-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(
                        jsonPath("$.title")
                                .value("12 km in einem Lauf")
                )
                .andExpect(
                        jsonPath("$.metric")
                                .value("RUNNING_DISTANCE_KM")
                )
                .andExpect(jsonPath("$.targetValue").value(12))
                .andExpect(jsonPath("$.progressPercent").value(0))
                .andExpect(jsonPath("$.completed").value(false));

        mockMvc.perform(get("/api/training-goals"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(
                        jsonPath("$[0].title")
                                .value("12 km in einem Lauf")
                );

        assertEquals(1, goalRepository.count());
    }

    @Test
    void calculatesRunningDistanceProgress() throws Exception {
        String trainingBody = """
                {
                    "type": "RUNNING",
                    "trainingDate": "2026-09-01",
                    "durationMinutes": 30,
                    "notes": "Sechs Kilometer",
                    "runningDetails": {
                        "runType": "EASY",
                        "distanceMeters": 6000,
                        "elapsedSeconds": 1800,
                        "averageHeartRate": 145,
                        "maxHeartRate": 165
                    },
                    "boulderingResults": [],
                    "strengthExercises": []
                }
                """;

        mockMvc.perform(post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(trainingBody))
                .andExpect(status().isCreated());

        String goalBody = """
                {
                    "title": "12 km schaffen",
                    "metric": "RUNNING_DISTANCE_KM",
                    "targetValue": 12,
                    "targetDate": "2035-12-31"
                }
                """;

        mockMvc.perform(post("/api/training-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(goalBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.currentValue").value(6))
                .andExpect(jsonPath("$.progressPercent").value(50))
                .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void updatesAndDeletesTrainingGoal() throws Exception {
        String createBody = """
                {
                    "title": "V6 schaffen",
                    "metric": "BOULDERING_GRADE",
                    "targetValue": 6,
                    "targetDate": "2035-12-31"
                }
                """;

        mockMvc.perform(post("/api/training-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createBody))
                .andExpect(status().isCreated());

        Long goalId = goalRepository.findAll()
                .getFirst()
                .getId();

        String updateBody = """
                {
                    "title": "V7 schaffen",
                    "metric": "BOULDERING_GRADE",
                    "targetValue": 7,
                    "targetDate": "2035-12-31"
                }
                """;

        mockMvc.perform(put(
                                "/api/training-goals/{id}",
                                goalId
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(
                        jsonPath("$.title").value("V7 schaffen")
                )
                .andExpect(jsonPath("$.targetValue").value(7));

        mockMvc.perform(delete(
                        "/api/training-goals/{id}",
                        goalId
                ))
                .andExpect(status().isNoContent());

        assertEquals(0, goalRepository.count());
    }

    @Test
    void rejectsStrengthGoalWithoutExerciseName()
            throws Exception {

        String requestBody = """
                {
                    "title": "50 kg Klimmzug",
                    "metric": "STRENGTH_WEIGHT_KG",
                    "targetValue": 50,
                    "targetDate": "2035-12-31"
                }
                """;

        mockMvc.perform(post("/api/training-goals")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        assertEquals(0, goalRepository.count());
    }
}