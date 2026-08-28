from datetime import date
from enum import StrEnum

from pydantic import BaseModel, ConfigDict, Field


class TrainingType(StrEnum):
    RUNNING = "RUNNING"
    BOULDERING = "BOULDERING"
    STRENGTH = "STRENGTH"


class RunningDetails(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    run_type: str = Field(alias="runType")
    distance_meters: int = Field(alias="distanceMeters")
    elapsed_seconds: int = Field(alias="elapsedSeconds")
    pace_seconds_per_kilometer: int = Field(
        alias="paceSecondsPerKilometer"
    )
    average_heart_rate: int | None = Field(
        default=None,
        alias="averageHeartRate",
    )
    max_heart_rate: int | None = Field(
        default=None,
        alias="maxHeartRate",
    )


class BoulderingGradeResult(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    id: int
    grade: str
    attempted_count: int = Field(alias="attemptedCount")
    completed_count: int = Field(alias="completedCount")


class StrengthSet(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    id: int
    set_number: int = Field(alias="setNumber")
    repetitions: int
    weight_kg: float = Field(alias="weightKg")
    volume_kg: float = Field(alias="volumeKg")


class StrengthExercise(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    id: int
    exercise_name: str = Field(alias="exerciseName")
    exercise_order: int = Field(alias="exerciseOrder")
    sets: list[StrengthSet] = Field(default_factory=list)
    volume_kg: float = Field(alias="volumeKg")


class TrainingSession(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    id: int
    type: TrainingType
    training_date: date = Field(alias="trainingDate")
    duration_minutes: int = Field(alias="durationMinutes")
    notes: str | None = None

    running_details: RunningDetails | None = Field(
        default=None,
        alias="runningDetails",
    )
    bouldering_results: list[BoulderingGradeResult] = Field(
        default_factory=list,
        alias="boulderingResults",
    )
    strength_exercises: list[StrengthExercise] = Field(
        default_factory=list,
        alias="strengthExercises",
    )


class AnalyticsSummary(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    total_sessions: int = Field(alias="totalSessions")
    total_duration_minutes: int = Field(
        alias="totalDurationMinutes"
    )
    average_duration_minutes: float = Field(
        alias="averageDurationMinutes"
    )
    sessions_by_type: dict[str, int] = Field(
        alias="sessionsByType"
    )
    duration_by_type: dict[str, int] = Field(
        alias="durationByType"
    )

    total_running_distance_km: float = Field(
        alias="totalRunningDistanceKm"
    )
    average_running_pace_seconds_per_km: float | None = Field(
        alias="averageRunningPaceSecondsPerKm"
    )

    total_completed_boulders: int = Field(
        alias="totalCompletedBoulders"
    )
    completed_boulders_by_grade: dict[str, int] = Field(
        alias="completedBouldersByGrade"
    )

    total_strength_volume_kg: float = Field(
        alias="totalStrengthVolumeKg"
    )
    strength_volume_by_exercise: dict[str, float] = Field(
        alias="strengthVolumeByExercise"
    )
    highest_volume_exercise: str | None = Field(
        alias="highestVolumeExercise"
    )