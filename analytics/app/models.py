from datetime import date
from enum import StrEnum

from pydantic import BaseModel, ConfigDict, Field


class TrainingType(StrEnum):
    RUNNING = "RUNNING"
    BOULDERING = "BOULDERING"
    STRENGTH = "STRENGTH"


class TrainingSession(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    id: int
    type: TrainingType
    training_date: date = Field(alias="trainingDate")
    duration_minutes: int = Field(alias="durationMinutes", gt=0)
    notes: str | None = None


class AnalyticsSummary(BaseModel):
    model_config = ConfigDict(populate_by_name=True)

    total_sessions: int = Field(alias="totalSessions")
    total_duration_minutes: int = Field(alias="totalDurationMinutes")
    average_duration_minutes: float = Field(alias="averageDurationMinutes")
    sessions_by_type: dict[str, int] = Field(alias="sessionsByType")
    duration_by_type: dict[str, int] = Field(alias="durationByType")