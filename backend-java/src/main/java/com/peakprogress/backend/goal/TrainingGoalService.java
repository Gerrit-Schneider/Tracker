package com.peakprogress.backend.goal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class TrainingGoalService {

    private static final BigDecimal MIN_BOULDERING_GRADE =
            BigDecimal.ONE;

    private static final BigDecimal MAX_BOULDERING_GRADE =
            BigDecimal.valueOf(17);

    private final TrainingGoalRepository repository;
    private final TrainingGoalProgressService progressService;

    public TrainingGoalService(
            TrainingGoalRepository repository,
            TrainingGoalProgressService progressService
    ) {
        this.repository = repository;
        this.progressService = progressService;
    }

    @Transactional
    public TrainingGoalResponse create(
            CreateTrainingGoalRequest request
    ) {
        validateGoal(
                request.title(),
                request.metric(),
                request.targetValue(),
                request.exerciseName()
        );

        TrainingGoal goal = new TrainingGoal(
                request.title(),
                request.metric(),
                request.targetValue(),
                request.exerciseName(),
                request.targetDate()
        );

        TrainingGoal savedGoal = repository.save(goal);

        return createResponse(savedGoal);
    }

    public List<TrainingGoalResponse> findAll() {
        List<TrainingGoal> goals =
                repository.findAllByOrderByCreatedAtDescIdDesc();

        Map<Long, TrainingGoalProgress> progressByGoal =
                progressService.calculateFor(goals);

        return goals.stream()
                .map(goal -> TrainingGoalResponse.from(
                        goal,
                        progressByGoal.getOrDefault(
                                goal.getId(),
                                TrainingGoalProgress.noData()
                        )
                ))
                .toList();
    }

    @Transactional
    public TrainingGoalResponse update(
            Long id,
            UpdateTrainingGoalRequest request
    ) {
        validateGoal(
                request.title(),
                request.metric(),
                request.targetValue(),
                request.exerciseName()
        );

        TrainingGoal goal = findGoal(id);

        goal.update(
                request.title(),
                request.metric(),
                request.targetValue(),
                request.exerciseName(),
                request.targetDate()
        );

        return createResponse(goal);
    }

    @Transactional
    public void delete(Long id) {
        TrainingGoal goal = findGoal(id);
        repository.delete(goal);
    }

    private TrainingGoalResponse createResponse(
            TrainingGoal goal
    ) {
        TrainingGoalProgress progress =
                progressService
                        .calculateFor(List.of(goal))
                        .getOrDefault(
                                goal.getId(),
                                TrainingGoalProgress.noData()
                        );

        return TrainingGoalResponse.from(goal, progress);
    }

    private TrainingGoal findGoal(Long id) {
        return repository.findById(id)
                .orElseThrow(
                        () -> new TrainingGoalNotFoundException(id)
                );
    }

    private void validateGoal(
            String title,
            GoalMetric metric,
            BigDecimal targetValue,
            String exerciseName
    ) {
        if (title == null || title.isBlank()) {
            throw new InvalidTrainingGoalException(
                    "Der Titel darf nicht leer sein."
            );
        }

        if (metric == null) {
            throw new InvalidTrainingGoalException(
                    "Eine Zielmetrik muss ausgewählt werden."
            );
        }

        if (targetValue == null
                || targetValue.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTrainingGoalException(
                    "Der Zielwert muss größer als 0 sein."
            );
        }

        boolean hasExerciseName =
                exerciseName != null && !exerciseName.isBlank();

        if (metric == GoalMetric.STRENGTH_WEIGHT_KG
                && !hasExerciseName) {
            throw new InvalidTrainingGoalException(
                    "Für ein Kraftziel muss eine Übung angegeben werden."
            );
        }

        if (metric != GoalMetric.STRENGTH_WEIGHT_KG
                && hasExerciseName) {
            throw new InvalidTrainingGoalException(
                    "Eine Übung ist nur bei einem Kraftziel erlaubt."
            );
        }

        if (metric == GoalMetric.BOULDERING_GRADE) {
            validateBoulderingGrade(targetValue);
        }
    }

    private void validateBoulderingGrade(
            BigDecimal targetValue
    ) {
        boolean outsideRange =
                targetValue.compareTo(MIN_BOULDERING_GRADE) < 0
                        || targetValue.compareTo(
                                MAX_BOULDERING_GRADE
                        ) > 0;

        boolean isNotWholeNumber =
                targetValue.stripTrailingZeros().scale() > 0;

        if (outsideRange || isNotWholeNumber) {
            throw new InvalidTrainingGoalException(
                    "Der Bouldering-Zielgrad muss eine ganze Zahl "
                            + "zwischen 1 und 17 sein."
            );
        }
    }
}