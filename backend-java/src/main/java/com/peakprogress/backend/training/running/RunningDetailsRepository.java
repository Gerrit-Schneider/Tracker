package com.peakprogress.backend.training.running;

import org.springframework.data.jpa.repository.JpaRepository;

public interface RunningDetailsRepository
        extends JpaRepository<RunningDetails, Long> {
}