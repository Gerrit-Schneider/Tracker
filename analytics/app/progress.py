from collections import defaultdict
from datetime import date

from app.models import AnalyticsProgressPoint, TrainingSession


def calculate_progress(
    sessions: list[TrainingSession],
) -> list[AnalyticsProgressPoint]:
    sessions_by_date: dict[date, list[TrainingSession]] = (
        defaultdict(list)
    )

    for session in sessions:
        sessions_by_date[session.training_date].append(session)

    progress_points: list[AnalyticsProgressPoint] = []

    for training_date in sorted(sessions_by_date):
        daily_sessions = sessions_by_date[training_date]

        running_distance_meters = sum(
            session.running_details.distance_meters
            for session in daily_sessions
            if session.running_details is not None
        )

        running_elapsed_seconds = sum(
            session.running_details.elapsed_seconds
            for session in daily_sessions
            if session.running_details is not None
        )

        average_running_pace = (
            round(
                running_elapsed_seconds
                / running_distance_meters
                * 1000
            )
            if running_distance_meters > 0
            else None
        )

        completed_boulders = sum(
            result.completed_count
            for session in daily_sessions
            for result in session.bouldering_results
        )

        strength_volume = sum(
            exercise.volume_kg
            for session in daily_sessions
            for exercise in session.strength_exercises
        )

        progress_points.append(
            AnalyticsProgressPoint(
                training_date=training_date,
                session_count=len(daily_sessions),
                total_duration_minutes=sum(
                    session.duration_minutes
                    for session in daily_sessions
                ),
                running_distance_km=round(
                    running_distance_meters / 1000,
                    2,
                ),
                average_running_pace_seconds_per_km=(
                    average_running_pace
                ),
                completed_boulders=completed_boulders,
                strength_volume_kg=round(
                    strength_volume,
                    2,
                ),
            )
        )

    return progress_points