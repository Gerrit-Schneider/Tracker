from datetime import date

from app.models import (
    BoulderingGradeResult,
    RunningDetails,
    StrengthExercise,
    StrengthSet,
    TrainingSession,
    TrainingType,
)
from app.summary import calculate_summary


def test_calculate_detailed_summary() -> None:
    sessions = [
        TrainingSession(
            id=1,
            type=TrainingType.RUNNING,
            training_date=date(2026, 8, 20),
            duration_minutes=26,
            notes="Easy run",
            running_details=RunningDetails(
                run_type="EASY",
                distance_meters=5000,
                elapsed_seconds=1560,
                pace_seconds_per_kilometer=312,
                average_heart_rate=148,
                max_heart_rate=169,
            ),
        ),
        TrainingSession(
            id=2,
            type=TrainingType.RUNNING,
            training_date=date(2026, 8, 21),
            duration_minutes=50,
            notes="Tempo run",
            running_details=RunningDetails(
                run_type="TEMPO",
                distance_meters=10000,
                elapsed_seconds=3000,
                pace_seconds_per_kilometer=300,
            ),
        ),
        TrainingSession(
            id=3,
            type=TrainingType.BOULDERING,
            training_date=date(2026, 8, 22),
            duration_minutes=90,
            notes="Bouldering",
            bouldering_results=[
                BoulderingGradeResult(
                    id=1,
                    grade="V4",
                    attempted_count=6,
                    completed_count=3,
                ),
                BoulderingGradeResult(
                    id=2,
                    grade="V5",
                    attempted_count=4,
                    completed_count=1,
                ),
            ],
        ),
        TrainingSession(
            id=4,
            type=TrainingType.STRENGTH,
            training_date=date(2026, 8, 23),
            duration_minutes=75,
            notes="Strength",
            strength_exercises=[
                StrengthExercise(
                    id=1,
                    exercise_name="Kreuzheben",
                    exercise_order=1,
                    sets=[
                        StrengthSet(
                            id=1,
                            set_number=1,
                            repetitions=5,
                            weight_kg=120,
                            volume_kg=600,
                        ),
                        StrengthSet(
                            id=2,
                            set_number=2,
                            repetitions=5,
                            weight_kg=120,
                            volume_kg=600,
                        ),
                        StrengthSet(
                            id=3,
                            set_number=3,
                            repetitions=4,
                            weight_kg=120,
                            volume_kg=480,
                        ),
                    ],
                    volume_kg=1680,
                ),
                StrengthExercise(
                    id=2,
                    exercise_name="Klimmzüge",
                    exercise_order=2,
                    sets=[
                        StrengthSet(
                            id=4,
                            set_number=1,
                            repetitions=2,
                            weight_kg=45,
                            volume_kg=90,
                        ),
                        StrengthSet(
                            id=5,
                            set_number=2,
                            repetitions=2,
                            weight_kg=45,
                            volume_kg=90,
                        ),
                    ],
                    volume_kg=180,
                ),
            ],
        ),
    ]

    summary = calculate_summary(sessions)

    assert summary.total_sessions == 4
    assert summary.total_duration_minutes == 241
    assert summary.average_duration_minutes == 60.25

    assert summary.sessions_by_type == {
        "RUNNING": 2,
        "BOULDERING": 1,
        "STRENGTH": 1,
    }
    assert summary.duration_by_type == {
        "RUNNING": 76,
        "BOULDERING": 90,
        "STRENGTH": 75,
    }

    assert summary.total_running_distance_km == 15.0
    assert summary.average_running_pace_seconds_per_km == 304.0

    assert summary.total_completed_boulders == 4
    assert summary.completed_boulders_by_grade == {
        "V4": 3,
        "V5": 1,
    }

    assert summary.total_strength_volume_kg == 1860.0
    assert summary.strength_volume_by_exercise == {
        "Kreuzheben": 1680.0,
        "Klimmzüge": 180.0,
    }
    assert summary.highest_volume_exercise == "Kreuzheben"


def test_calculate_empty_summary() -> None:
    summary = calculate_summary([])

    assert summary.total_sessions == 0
    assert summary.total_duration_minutes == 0
    assert summary.average_duration_minutes == 0.0

    assert summary.sessions_by_type == {
        "RUNNING": 0,
        "BOULDERING": 0,
        "STRENGTH": 0,
    }
    assert summary.duration_by_type == {
        "RUNNING": 0,
        "BOULDERING": 0,
        "STRENGTH": 0,
    }

    assert summary.total_running_distance_km == 0.0
    assert summary.average_running_pace_seconds_per_km is None

    assert summary.total_completed_boulders == 0
    assert summary.completed_boulders_by_grade == {}

    assert summary.total_strength_volume_kg == 0.0
    assert summary.strength_volume_by_exercise == {}
    assert summary.highest_volume_exercise is None