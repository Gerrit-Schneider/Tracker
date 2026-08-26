package com.peakprogress.backend.training.bouldering;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BoulderingGradeResultRepository
        extends JpaRepository<BoulderingGradeResult, Long> {

    List<BoulderingGradeResult> findAllBySession_Id(Long sessionId);

    void deleteAllBySession_Id(Long sessionId);
}