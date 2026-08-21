package com.peakprogress.backend;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;

import com.peakprogress.backend.training.TrainingSession;
import com.peakprogress.backend.training.TrainingSessionRepository;
import com.peakprogress.backend.training.TrainingType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

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
@Test
void deletesTrainingSession() throws Exception {
    TrainingSession session = repository.save(
            new TrainingSession(
                    TrainingType.BOULDERING,
                    LocalDate.of(2026, 8, 20),
                    90,
                    "Testeinheit"
            )
    );

    mockMvc.perform(
                    delete(
                            "/api/training-sessions/{id}",
                            session.getId()
                    )
            )
            .andExpect(status().isNoContent());

    assertEquals(0, repository.count());
}

@Test
void returnsNotFoundWhenDeletingUnknownSession() throws Exception {
    mockMvc.perform(
                    delete("/api/training-sessions/{id}", 999999)
            )
            .andExpect(status().isNotFound());
}
@Test
void updatesTrainingSession() throws Exception {
    TrainingSession session = repository.save(
            new TrainingSession(
                    TrainingType.RUNNING,
                    LocalDate.of(2026, 8, 20),
                    30,
                    "Lockerer Lauf"
            )
    );

    String requestBody = """
            {
                "type": "RUNNING",
                "trainingDate": "2026-08-20",
                "durationMinutes": 45,
                "notes": "Schneller Dauerlauf"
            }
            """;

    mockMvc.perform(
                    put(
                            "/api/training-sessions/{id}",
                            session.getId()
                    )
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.durationMinutes").value(45))
            .andExpect(jsonPath("$.notes")
                    .value("Schneller Dauerlauf"));

    TrainingSession updatedSession = repository
            .findById(session.getId())
            .orElseThrow();

    assertEquals(45, updatedSession.getDurationMinutes());
    assertEquals("Schneller Dauerlauf", updatedSession.getNotes());
}
}