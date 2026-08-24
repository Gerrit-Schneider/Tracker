import httpx
from fastapi import FastAPI, HTTPException

from app.client import fetch_training_sessions
from app.models import AnalyticsSummary
from app.summary import calculate_summary
from fastapi.middleware.cors import CORSMiddleware

app = FastAPI(
    title="PeakProgress Analytics API",
    version="0.1.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:5173",
        "http://127.0.0.1:5173",
    ],
    allow_methods=["GET"],
    allow_headers=["*"],
)

@app.get("/health")
def health_check() -> dict[str, str]:
    return {
        "status": "ok",
        "service": "analytics",
    }


@app.get(
    "/api/analytics/summary",
    response_model=AnalyticsSummary,
)
async def get_analytics_summary() -> AnalyticsSummary:
    try:
        sessions = await fetch_training_sessions()
    except (httpx.HTTPError, ValueError) as error:
        raise HTTPException(
            status_code=503,
            detail="Training backend is unavailable",
        ) from error

    return calculate_summary(sessions)