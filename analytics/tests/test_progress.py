from datetime import date

from app.models import (
    BoulderingGradeResult,
    RunningDetails,
    StrengthExercise,
    TrainingSession,
    TrainingType,
)
from app.progress import calculate_progress


def test_calculate_progress_by_date() -> None:
    sessions = [
        TrainingSession(
            id=3,
            type=TrainingType.STRENGTH,
            training_date=date(2026, 8, 20),
            duration_minutes=45,
            strength_exercises=[
                StrengthExercise(
                    id=1,
                    exercise_name="Deadlift",
                    exercise_order=1,
                    volume_kg=1200,
                )
            ],
        ),
        TrainingSession(
            id=1,
            type=TrainingType.RUNNING,
            training_date=date(2026, 8, 19),
            duration_minutes=30,
            running_details=RunningDetails(
                run_type="EASY",
                distance_meters=5000,
                elapsed_seconds=1500,
                pace_seconds_per_kilometer=300,
            ),
        ),
        TrainingSession(
            id=2,
            type=TrainingType.BOULDERING,
            training_date=date(2026, 8, 20),
            duration_minutes=60,
            bouldering_results=[
                BoulderingGradeResult(
                    id=1,
                    grade="V4",
                    attempted_count=5,
                    completed_count=3,
                )
            ],
        ),
    ]

    progress = calculate_progress(sessions)

    assert len(progress) == 2

    running_day = progress[0]

    assert running_day.training_date == date(2026, 8, 19)
    assert running_day.session_count == 1
    assert running_day.total_duration_minutes == 30
    assert running_day.running_distance_km == 5.0
    assert (
        running_day.average_running_pace_seconds_per_km
        == 300
    )

    mixed_day = progress[1]

    assert mixed_day.training_date == date(2026, 8, 20)
    assert mixed_day.session_count == 2
    assert mixed_day.total_duration_minutes == 105
    assert mixed_day.completed_boulders == 3
    assert mixed_day.strength_volume_kg == 1200