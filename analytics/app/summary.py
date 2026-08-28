from app.models import (
    AnalyticsSummary,
    TrainingSession,
    TrainingType,
)


def bouldering_grade_order(grade: str) -> int:
    if grade == "VB":
        return -1

    if grade.startswith("V") and grade[1:].isdigit():
        return int(grade[1:])

    return 999


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

    total_running_distance_meters = 0
    total_running_elapsed_seconds = 0

    completed_boulders_by_grade: dict[str, int] = {}

    total_strength_volume_kg = 0.0
    strength_volume_by_exercise: dict[str, float] = {}

    for session in sessions:
        sessions_by_type[session.type.value] += 1
        duration_by_type[session.type.value] += (
            session.duration_minutes
        )

        if session.running_details is not None:
            total_running_distance_meters += (
                session.running_details.distance_meters
            )
            total_running_elapsed_seconds += (
                session.running_details.elapsed_seconds
            )

        for result in session.bouldering_results:
            completed_boulders_by_grade[result.grade] = (
                completed_boulders_by_grade.get(result.grade, 0)
                + result.completed_count
            )

        for exercise in session.strength_exercises:
            exercise_name = exercise.exercise_name.strip()
            exercise_volume = exercise.volume_kg

            total_strength_volume_kg += exercise_volume

            strength_volume_by_exercise[exercise_name] = (
                strength_volume_by_exercise.get(
                    exercise_name,
                    0.0,
                )
                + exercise_volume
            )

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

    total_running_distance_km = round(
        total_running_distance_meters / 1000,
        3,
    )

    average_running_pace = (
        round(
            total_running_elapsed_seconds
            / total_running_distance_km,
            2,
        )
        if total_running_distance_km > 0
        else None
    )

    completed_boulders_by_grade = dict(
        sorted(
            completed_boulders_by_grade.items(),
            key=lambda item: bouldering_grade_order(item[0]),
        )
    )

    total_completed_boulders = sum(
        completed_boulders_by_grade.values()
    )

    strength_volume_by_exercise = {
        exercise_name: round(volume, 2)
        for exercise_name, volume in
        strength_volume_by_exercise.items()
    }

    highest_volume_exercise = (
        max(
            strength_volume_by_exercise,
            key=strength_volume_by_exercise.get,
        )
        if strength_volume_by_exercise
        else None
    )

    return AnalyticsSummary(
        total_sessions=total_sessions,
        total_duration_minutes=total_duration,
        average_duration_minutes=average_duration,
        sessions_by_type=sessions_by_type,
        duration_by_type=duration_by_type,
        total_running_distance_km=total_running_distance_km,
        average_running_pace_seconds_per_km=(
            average_running_pace
        ),
        total_completed_boulders=total_completed_boulders,
        completed_boulders_by_grade=(
            completed_boulders_by_grade
        ),
        total_strength_volume_kg=round(
            total_strength_volume_kg,
            2,
        ),
        strength_volume_by_exercise=(
            strength_volume_by_exercise
        ),
        highest_volume_exercise=highest_volume_exercise,
    )