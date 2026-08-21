package com.peakprogress.backend.training;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrainingSessionService {

    private final TrainingSessionRepository repository;

    public TrainingSessionService(
            TrainingSessionRepository repository
    ) {
        this.repository = repository;
    }

    @Transactional
    public TrainingSessionResponse create(
            CreateTrainingSessionRequest request
    ) {
        TrainingSession session = new TrainingSession(
                request.type(),
                request.trainingDate(),
                request.durationMinutes(),
                request.notes()
        );

        return TrainingSessionResponse.from(
                repository.save(session)
        );
    }

    public List<TrainingSessionResponse> findAll(
            TrainingType type
    ) {
        List<TrainingSession> sessions = type == null
                ? repository.findAllByOrderByTrainingDateDesc()
                : repository.findByTypeOrderByTrainingDateDesc(type);

        return sessions.stream()
                .map(TrainingSessionResponse::from)
                .toList();
    }
}