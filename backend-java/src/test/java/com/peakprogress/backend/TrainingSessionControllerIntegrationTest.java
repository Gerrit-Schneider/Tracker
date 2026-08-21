package com.peakprogress.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.peakprogress.backend.training.TrainingSessionRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TrainingSessionControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TrainingSessionRepository repository;

    @BeforeEach
    void clearDatabase() {
        repository.deleteAll();
    }

    @Test
    void createsAndListsTrainingSession() throws Exception {
        String requestBody = """
                {
                    "type": "RUNNING",
                    "trainingDate": "2026-08-20",
                    "durationMinutes": 30,
                    "notes": "Lockerer Testlauf"
                }
                """;

        mockMvc.perform(post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.type").value("RUNNING"))
                .andExpect(jsonPath("$.durationMinutes").value(30));

        mockMvc.perform(get("/api/training-sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("RUNNING"))
                .andExpect(jsonPath("$[0].notes")
                        .value("Lockerer Testlauf"));

        assertEquals(1, repository.count());
    }

    @Test
    void rejectsTrainingSessionWithZeroDuration() throws Exception {
        String requestBody = """
                {
                    "type": "RUNNING",
                    "trainingDate": "2026-08-20",
                    "durationMinutes": 0
                }
                """;

        mockMvc.perform(post("/api/training-sessions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());

        assertEquals(0, repository.count());
    }
}