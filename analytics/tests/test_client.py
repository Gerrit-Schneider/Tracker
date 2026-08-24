import httpx
import pytest
import respx

from app.client import fetch_training_sessions
from app.models import TrainingType


@pytest.mark.asyncio
async def test_fetch_training_sessions() -> None:
    with respx.mock:
        respx.get(
            "http://localhost:8080/api/training-sessions"
        ).mock(
            return_value=httpx.Response(
                200,
                json=[
                    {
                        "id": 1,
                        "type": "RUNNING",
                        "trainingDate": "2026-08-21",
                        "durationMinutes": 45,
                        "notes": "Easy run",
                    }
                ],
            )
        )

        sessions = await fetch_training_sessions()

    assert len(sessions) == 1
    assert sessions[0].type == TrainingType.RUNNING
    assert sessions[0].duration_minutes == 45


@pytest.mark.asyncio
async def test_fetch_training_sessions_rejects_backend_error() -> None:
    with respx.mock:
        respx.get(
            "http://localhost:8080/api/training-sessions"
        ).mock(
            return_value=httpx.Response(500)
        )

        with pytest.raises(httpx.HTTPStatusError):
            await fetch_training_sessions()