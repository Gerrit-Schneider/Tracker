FROM python:3.12-slim

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

WORKDIR /app

RUN groupadd --system peakprogress \
    && useradd --system --gid peakprogress peakprogress

COPY analytics/requirements.txt ./requirements.txt

RUN pip install --no-cache-dir \
    -r requirements.txt

COPY --chown=peakprogress:peakprogress \
    analytics/app \
    ./app

USER peakprogress

EXPOSE 8000

CMD ["python", "-m", "uvicorn", "app.main:app", "--host", "0.0.0.0", "--port", "8000"]