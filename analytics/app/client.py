import os

import httpx

from app.models import TrainingSession


async def fetch_training_sessions() -> list[TrainingSession]:
    backend_url = os.getenv(
        "TRAINING_API_URL",
        "http://localhost:8080",
    ).rstrip("/")

    async with httpx.AsyncClient(timeout=5.0) as client:
        response = await client.get(
            f"{backend_url}/api/training-sessions"
        )
        response.raise_for_status()

    return [
        TrainingSession.model_validate(session)
        for session in response.json()
    ]