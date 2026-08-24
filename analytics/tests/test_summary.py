from datetime import date

from app.models import TrainingSession, TrainingType
from app.summary import calculate_summary


def test_calculate_summary() -> None:
    sessions = [
        TrainingSession(
            id=1,
            type=TrainingType.RUNNING,
            training_date=date(2026, 8, 20),
            duration_minutes=30,
            notes="Easy run",
        ),
        TrainingSession(
            id=2,
            type=TrainingType.STRENGTH,
            training_date=date(2026, 8, 21),
            duration_minutes=60,
            notes="Weighted pull-ups",
        ),
    ]

    summary = calculate_summary(sessions)

    assert summary.total_sessions == 2
    assert summary.total_duration_minutes == 90
    assert summary.average_duration_minutes == 45.0
    assert summary.sessions_by_type == {
        "RUNNING": 1,
        "BOULDERING": 0,
        "STRENGTH": 1,
    }


def test_calculate_empty_summary() -> None:
    summary = calculate_summary([])

    assert summary.total_sessions == 0
    assert summary.total_duration_minutes == 0
    assert summary.average_duration_minutes == 0.0