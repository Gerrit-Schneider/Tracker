package com.peakprogress.backend.training.gym;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface StrengthExerciseRepository
        extends JpaRepository<StrengthExercise, Long> {

    @EntityGraph(attributePaths = "sets")
    List<StrengthExercise>
    findAllBySession_IdOrderByExerciseOrderAsc(Long sessionId);

    void deleteAllBySession_Id(Long sessionId);
}