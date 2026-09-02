package com.peakprogress.backend.training;

import com.peakprogress.backend.training.bouldering.BoulderingGradeResult;
import com.peakprogress.backend.training.bouldering.BoulderingGradeResultRepository;
import com.peakprogress.backend.training.bouldering.BoulderingGradeResultRequest;
import com.peakprogress.backend.training.running.RunningDetails;
import com.peakprogress.backend.training.running.RunningDetailsRepository;
import com.peakprogress.backend.training.running.RunningDetailsRequest;
import com.peakprogress.backend.training.gym.StrengthExercise;
import com.peakprogress.backend.training.gym.StrengthExerciseRepository;
import com.peakprogress.backend.training.gym.StrengthExerciseRequest;
import com.peakprogress.backend.training.gym.StrengthSet;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Locale;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class TrainingSessionService {

    private final TrainingSessionRepository sessionRepository;
    private final RunningDetailsRepository runningRepository;
    private final BoulderingGradeResultRepository boulderingRepository;
    private final StrengthExerciseRepository strengthRepository;

    public TrainingSessionService(
            TrainingSessionRepository sessionRepository,
            RunningDetailsRepository runningRepository,
            BoulderingGradeResultRepository boulderingRepository,
            StrengthExerciseRepository strengthRepository
    ) {
        this.sessionRepository = sessionRepository;
        this.runningRepository = runningRepository;
        this.boulderingRepository = boulderingRepository;
        this.strengthRepository = strengthRepository;
    }

    @Transactional
    public TrainingSessionResponse create(
            CreateTrainingSessionRequest request
    ) {
        validateDetails(
                request.type(),
                request.runningDetails(),
                request.boulderingResults(),
                request.strengthExercises()
        );

        TrainingSession session = new TrainingSession(
                request.type(),
                request.trainingDate(),
                request.durationMinutes(),
                request.notes()
        );

        sessionRepository.save(session);

        saveDetails(
                session,
                request.runningDetails(),
                request.boulderingResults(),
                request.strengthExercises()
        );

        return createResponse(session);
    }

    public List<TrainingSessionResponse> findAll(
            TrainingType type
    ) {
        List<TrainingSession> sessions = type == null
                ? sessionRepository.findAllByOrderByTrainingDateDesc()
                : sessionRepository.findByTypeOrderByTrainingDateDesc(type);

        return sessions.stream()
                .map(this::createResponse)
                .toList();
    }

public TrainingSessionPageResponse search(
        TrainingType type,
        LocalDate from,
        LocalDate to,
        String query,
        int page,
        int size
) {
    Specification<TrainingSession> specification =
            Specification.unrestricted();

    if (type != null) {
        specification = specification.and(
                (root, criteriaQuery, builder) ->
                        builder.equal(
                                root.<TrainingType>get("type"),
                                type
                        )
        );
    }

    if (from != null) {
        specification = specification.and(
                (root, criteriaQuery, builder) ->
                        builder.greaterThanOrEqualTo(
                                root.<LocalDate>get("trainingDate"),
                                from
                        )
        );
    }

    if (to != null) {
        specification = specification.and(
                (root, criteriaQuery, builder) ->
                        builder.lessThanOrEqualTo(
                                root.<LocalDate>get("trainingDate"),
                                to
                        )
        );
    }

    if (query != null && !query.isBlank()) {
        String searchTerm = "%"
                + query.trim().toLowerCase(Locale.ROOT)
                + "%";

        specification = specification.and(
                (root, criteriaQuery, builder) ->
                        builder.like(
                                builder.lower(
                                        root.<String>get("notes")
                                ),
                                searchTerm
                        )
        );
    }

    PageRequest pageRequest = PageRequest.of(
            page,
            size,
            Sort.by(
                    Sort.Direction.DESC,
                    "trainingDate"
            ).and(
                    Sort.by(
                            Sort.Direction.DESC,
                            "id"
                    )
            )
    );

    Page<TrainingSessionResponse> result = sessionRepository
            .findAll(specification, pageRequest)
            .map(this::createResponse);

    return TrainingSessionPageResponse.from(result);
}

    @Transactional
    public TrainingSessionResponse update(
            Long id,
            UpdateTrainingSessionRequest request
    ) {
        validateDetails(
                request.type(),
                request.runningDetails(),
                request.boulderingResults(),
                request.strengthExercises()
        );

        TrainingSession session = findSession(id);

        removeDetails(id);

        session.update(
                request.type(),
                request.trainingDate(),
                request.durationMinutes(),
                request.notes()
        );

        saveDetails(
                session,
                request.runningDetails(),
                request.boulderingResults(),
                request.strengthExercises()
        );

        return createResponse(session);
    }

    @Transactional
    public void delete(Long id) {
        TrainingSession session = findSession(id);
        sessionRepository.delete(session);
    }

    private TrainingSession findSession(Long id) {
        return sessionRepository.findById(id)
                .orElseThrow(
                        () -> new TrainingSessionNotFoundException(id)
                );
    }

    private void saveDetails(
            TrainingSession session,
            RunningDetailsRequest runningRequest,
            List<BoulderingGradeResultRequest> boulderingRequests,
            List<StrengthExerciseRequest> strengthRequests
    ) {
        saveRunningDetails(session, runningRequest);
        saveBoulderingDetails(session, boulderingRequests);
        saveStrengthDetails(session, strengthRequests);
    }

    private void saveRunningDetails(
            TrainingSession session,
            RunningDetailsRequest request
    ) {
        if (request == null) {
            return;
        }

        RunningDetails details = new RunningDetails(
                session,
                request.runType(),
                request.distanceMeters(),
                request.elapsedSeconds(),
                request.averageHeartRate(),
                request.maxHeartRate()
        );

        runningRepository.save(details);
    }

    private void saveBoulderingDetails(
            TrainingSession session,
            List<BoulderingGradeResultRequest> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<BoulderingGradeResult> results = requests.stream()
                .map(request -> new BoulderingGradeResult(
                        session,
                        request.grade(),
                        request.attemptedCount(),
                        request.completedCount()
                ))
                .toList();

        boulderingRepository.saveAll(results);
    }

    private void saveStrengthDetails(
            TrainingSession session,
            List<StrengthExerciseRequest> requests
    ) {
        if (requests == null || requests.isEmpty()) {
            return;
        }

        List<StrengthExercise> exercises = new ArrayList<>();

        for (int exerciseIndex = 0;
             exerciseIndex < requests.size();
             exerciseIndex++) {

            StrengthExerciseRequest request =
                    requests.get(exerciseIndex);

            StrengthExercise exercise = new StrengthExercise(
                    session,
                    request.exerciseName().trim(),
                    exerciseIndex + 1
            );

            for (int setIndex = 0;
                 setIndex < request.sets().size();
                 setIndex++) {

                var setRequest = request.sets().get(setIndex);

                exercise.addSet(
                        new StrengthSet(
                                setIndex + 1,
                                setRequest.repetitions(),
                                setRequest.weightKg()
                        )
                );
            }

            exercises.add(exercise);
        }

        strengthRepository.saveAll(exercises);
    }

    private void removeDetails(Long sessionId) {
        runningRepository.findById(sessionId)
                .ifPresent(runningRepository::delete);

        boulderingRepository.deleteAllBySession_Id(sessionId);
        strengthRepository.deleteAllBySession_Id(sessionId);

        runningRepository.flush();
        boulderingRepository.flush();
        strengthRepository.flush();
    }

    private TrainingSessionResponse createResponse(
            TrainingSession session
    ) {
        RunningDetails runningDetails =
                runningRepository.findById(session.getId())
                        .orElse(null);

        List<BoulderingGradeResult> boulderingResults =
                boulderingRepository
                        .findAllBySession_Id(session.getId())
                        .stream()
                        .sorted(
                                Comparator.comparingInt(
                                        result ->
                                                result.getGrade().ordinal()
                                )
                        )
                        .toList();

        List<StrengthExercise> strengthExercises =
                strengthRepository
                        .findAllBySession_IdOrderByExerciseOrderAsc(
                                session.getId()
                        );

        return TrainingSessionResponse.from(
                session,
                runningDetails,
                boulderingResults,
                strengthExercises
        );
    }

    private void validateDetails(
            TrainingType type,
            RunningDetailsRequest runningDetails,
            List<BoulderingGradeResultRequest> boulderingResults,
            List<StrengthExerciseRequest> strengthExercises
    ) {
        boolean hasBoulderingDetails =
                boulderingResults != null
                        && !boulderingResults.isEmpty();

        boolean hasStrengthDetails =
                strengthExercises != null
                        && !strengthExercises.isEmpty();

        if (type != TrainingType.RUNNING && runningDetails != null) {
            throw new InvalidTrainingDetailsException(
                    "Laufdetails sind nur bei RUNNING erlaubt."
            );
        }

        if (type != TrainingType.BOULDERING
                && hasBoulderingDetails) {
            throw new InvalidTrainingDetailsException(
                    "Bouldering-Details sind nur bei BOULDERING erlaubt."
            );
        }

        if (type != TrainingType.STRENGTH && hasStrengthDetails) {
            throw new InvalidTrainingDetailsException(
                    "Kraftübungen sind nur bei Gym erlaubt."
            );
        }

        if (hasBoulderingDetails) {
            long distinctGrades = boulderingResults.stream()
                    .map(BoulderingGradeResultRequest::grade)
                    .distinct()
                    .count();

            if (distinctGrades != boulderingResults.size()) {
                throw new InvalidTrainingDetailsException(
                        "Jeder Bouldering-Grad darf nur einmal vorkommen."
                );
            }
        }
    }
}