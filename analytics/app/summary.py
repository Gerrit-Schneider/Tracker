from app.models import AnalyticsSummary, TrainingSession, TrainingType


def calculate_summary(
    sessions: list[TrainingSession],
) -> AnalyticsSummary:
    sessions_by_type = {
        training_type.value: 0
        for training_type in TrainingType
    }
    duration_by_type = {
        training_type.value: 0
        for training_type in TrainingType
    }

    for session in sessions:
        sessions_by_type[session.type.value] += 1
        duration_by_type[session.type.value] += session.duration_minutes

    total_sessions = len(sessions)
    total_duration = sum(
        session.duration_minutes
        for session in sessions
    )

    average_duration = (
        round(total_duration / total_sessions, 2)
        if total_sessions > 0
        else 0.0
    )

    return AnalyticsSummary(
        total_sessions=total_sessions,
        total_duration_minutes=total_duration,
        average_duration_minutes=average_duration,
        sessions_by_type=sessions_by_type,
        duration_by_type=duration_by_type,
    )