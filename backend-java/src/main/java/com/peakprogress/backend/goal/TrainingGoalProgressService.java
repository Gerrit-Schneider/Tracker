package com.peakprogress.backend.goal;

import com.peakprogress.backend.training.bouldering.BoulderingGrade;
import com.peakprogress.backend.training.bouldering.BoulderingGradeResultRepository;
import com.peakprogress.backend.training.gym.StrengthExercise;
import com.peakprogress.backend.training.gym.StrengthExerciseRepository;
import com.peakprogress.backend.training.running.RunningDetails;
import com.peakprogress.backend.training.running.RunningDetailsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
@Transactional(readOnly = true)
public class TrainingGoalProgressService {

    private static final BigDecimal ONE_HUNDRED =
            BigDecimal.valueOf(100);

    private static final BigDecimal ONE_THOUSAND =
            BigDecimal.valueOf(1000);

    private final RunningDetailsRepository runningRepository;
    private final BoulderingGradeResultRepository boulderingRepository;
    private final StrengthExerciseRepository strengthRepository;

    public TrainingGoalProgressService(
            RunningDetailsRepository runningRepository,
            BoulderingGradeResultRepository boulderingRepository,
            StrengthExerciseRepository strengthRepository
    ) {
        this.runningRepository = runningRepository;
        this.boulderingRepository = boulderingRepository;
        this.strengthRepository = strengthRepository;
    }

    public Map<Long, TrainingGoalProgress> calculateFor(
            List<TrainingGoal> goals
    ) {
        if (goals.isEmpty()) {
            return Map.of();
        }

        BigDecimal longestRunningDistance =
                hasMetric(goals, GoalMetric.RUNNING_DISTANCE_KM)
                        ? findLongestRunningDistance()
                        : null;

        BigDecimal fastestRunningPace =
                hasMetric(
                        goals,
                        GoalMetric.RUNNING_PACE_SECONDS_PER_KM
                )
                        ? findFastestRunningPace()
                        : null;

        BigDecimal highestBoulderingGrade =
                hasMetric(goals, GoalMetric.BOULDERING_GRADE)
                        ? findHighestCompletedBoulderingGrade()
                        : null;

        Map<String, BigDecimal> strengthWeights =
                hasMetric(goals, GoalMetric.STRENGTH_WEIGHT_KG)
                        ? findHighestStrengthWeights()
                        : Map.of();

        Map<Long, TrainingGoalProgress> progressByGoal =
                new HashMap<>();

        for (TrainingGoal goal : goals) {
            BigDecimal currentValue = switch (goal.getMetric()) {
                case RUNNING_DISTANCE_KM ->
                        longestRunningDistance;

                case RUNNING_PACE_SECONDS_PER_KM ->
                        fastestRunningPace;

                case BOULDERING_GRADE ->
                        highestBoulderingGrade;

                case STRENGTH_WEIGHT_KG ->
                        strengthWeights.get(
                                normalizeExerciseName(
                                        goal.getExerciseName()
                                )
                        );
            };

            progressByGoal.put(
                    goal.getId(),
                    createProgress(goal, currentValue)
            );
        }

        return progressByGoal;
    }

    private boolean hasMetric(
            List<TrainingGoal> goals,
            GoalMetric metric
    ) {
        return goals.stream()
                .anyMatch(goal -> goal.getMetric() == metric);
    }

    private BigDecimal findLongestRunningDistance() {
        return runningRepository.findAll()
                .stream()
                .map(RunningDetails::getDistanceMeters)
                .map(BigDecimal::valueOf)
                .map(distance -> distance.divide(
                        ONE_THOUSAND,
                        2,
                        RoundingMode.HALF_UP
                ))
                .max(BigDecimal::compareTo)
                .orElse(null);
    }

    private BigDecimal findFastestRunningPace() {
        return runningRepository.findAll()
                .stream()
                .map(RunningDetails::getPaceSecondsPerKilometer)
                .map(BigDecimal::valueOf)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    private BigDecimal findHighestCompletedBoulderingGrade() {
        return boulderingRepository.findAll()
                .stream()
                .filter(result -> result.getCompletedCount() > 0)
                .map(result -> gradeToValue(result.getGrade()))
                .map(BigDecimal::valueOf)
                .max(BigDecimal::compareTo)
                .orElse(null);
    }

    private Map<String, BigDecimal> findHighestStrengthWeights() {
        Map<String, BigDecimal> highestWeights = new HashMap<>();

        for (StrengthExercise exercise
                : strengthRepository.findAll()) {

            String exerciseName = normalizeExerciseName(
                    exercise.getExerciseName()
            );

            exercise.getSets().stream()
                    .map(set -> set.getWeightKg())
                    .max(BigDecimal::compareTo)
                    .ifPresent(weight ->
                            highestWeights.merge(
                                    exerciseName,
                                    weight,
                                    BigDecimal::max
                            )
                    );
        }

        return highestWeights;
    }

    private TrainingGoalProgress createProgress(
            TrainingGoal goal,
            BigDecimal currentValue
    ) {
        if (currentValue == null
                || currentValue.compareTo(BigDecimal.ZERO) <= 0) {
            return TrainingGoalProgress.noData();
        }

        boolean paceGoal =
                goal.getMetric()
                        == GoalMetric.RUNNING_PACE_SECONDS_PER_KM;

        boolean completed = paceGoal
                ? currentValue.compareTo(goal.getTargetValue()) <= 0
                : currentValue.compareTo(goal.getTargetValue()) >= 0;

        BigDecimal progress = paceGoal
                ? goal.getTargetValue().divide(
                        currentValue,
                        6,
                        RoundingMode.HALF_UP
                )
                : currentValue.divide(
                        goal.getTargetValue(),
                        6,
                        RoundingMode.HALF_UP
                );

        BigDecimal progressPercent = progress
                .multiply(ONE_HUNDRED)
                .min(ONE_HUNDRED)
                .setScale(1, RoundingMode.HALF_UP);

        return new TrainingGoalProgress(
                currentValue,
                progressPercent,
                completed
        );
    }

    private int gradeToValue(BoulderingGrade grade) {
        if (grade == BoulderingGrade.VB
                || grade == BoulderingGrade.V0) {
            return 0;
        }

        return Integer.parseInt(
                grade.name().substring(1)
        );
    }

    private String normalizeExerciseName(String exerciseName) {
        if (exerciseName == null) {
            return "";
        }

        return exerciseName
                .trim()
                .toLowerCase(Locale.ROOT);
    }
}