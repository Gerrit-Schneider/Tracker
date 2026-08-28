import {
  useCallback,
  useEffect,
  useState,
} from 'react'
import {
  deleteTrainingSession,
  getTrainingSessions,
} from './api/trainingSessions'
import {
  getAnalyticsSummary,
  type AnalyticsSummary,
} from './api/analytics'
import { TrainingSessionForm } from './components/TrainingSessionForm'
import type {
  TrainingSession,
  TrainingType,
} from './types/training'
import './App.css'
import { TrainingSessionDetails } from './components/TrainingSessionDetails'
import { SportAnalytics } from './components/SportAnalytics'

const typeLabels: Record<TrainingType, string> = {
  RUNNING: 'Laufen',
  BOULDERING: 'Bouldern',
  STRENGTH: 'Krafttraining',
}

const trainingTypes: TrainingType[] = [
  'RUNNING',
  'BOULDERING',
  'STRENGTH',
]

function formatDate(date: string): string {
  return new Intl.DateTimeFormat('de-DE').format(
    new Date(`${date}T00:00:00`),
  )
}

function formatDuration(minutes: number): string {
  const hours = Math.floor(minutes / 60)
  const remainingMinutes = minutes % 60

  if (hours === 0) {
    return `${remainingMinutes} Min.`
  }

  if (remainingMinutes === 0) {
    return `${hours} Std.`
  }

  return `${hours} Std. ${remainingMinutes} Min.`
}

function App() {
  const [sessions, setSessions] = useState<TrainingSession[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [deletingId, setDeletingId] = useState<number | null>(null)
  const [editingSession, setEditingSession] =
    useState<TrainingSession | null>(null)

  const [analytics, setAnalytics] =
    useState<AnalyticsSummary | null>(null)
  const [analyticsLoading, setAnalyticsLoading] = useState(true)
  const [analyticsError, setAnalyticsError] =
    useState<string | null>(null)

  const loadAnalytics = useCallback(async () => {
    setAnalyticsLoading(true)
    setAnalyticsError(null)

    try {
      const result = await getAnalyticsSummary()
      setAnalytics(result)
    } catch (caughtError) {
      setAnalyticsError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Analytics konnten nicht geladen werden.',
      )
    } finally {
      setAnalyticsLoading(false)
    }
  }, [])

  useEffect(() => {
    async function loadSessions() {
      try {
        const result = await getTrainingSessions()
        setSessions(result)
      } catch (caughtError) {
        setError(
          caughtError instanceof Error
            ? caughtError.message
            : 'Ein unbekannter Fehler ist aufgetreten.',
        )
      } finally {
        setLoading(false)
      }
    }

    void loadSessions()
    void loadAnalytics()
  }, [loadAnalytics])

  function handleSessionCreated(session: TrainingSession) {
    setSessions((currentSessions) =>
      [...currentSessions, session].sort((first, second) =>
        second.trainingDate.localeCompare(first.trainingDate),
      ),
    )

    void loadAnalytics()
  }

  function handleSessionUpdated(updatedSession: TrainingSession) {
    setSessions((currentSessions) =>
      currentSessions
        .map((session) =>
          session.id === updatedSession.id
            ? updatedSession
            : session,
        )
        .sort((first, second) =>
          second.trainingDate.localeCompare(first.trainingDate),
        ),
    )

    setEditingSession(null)
    void loadAnalytics()
  }

  async function handleSessionDeleted(id: number) {
    const confirmed = window.confirm(
      'Möchtest du diese Trainingseinheit wirklich löschen?',
    )

    if (!confirmed) {
      return
    }

    setDeletingId(id)
    setError(null)

    try {
      await deleteTrainingSession(id)

      setSessions((currentSessions) =>
        currentSessions.filter((session) => session.id !== id),
      )

      setEditingSession((currentSession) =>
        currentSession?.id === id ? null : currentSession,
      )

      void loadAnalytics()
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Ein unbekannter Fehler ist aufgetreten.',
      )
    } finally {
      setDeletingId(null)
    }
  }

  return (
    <main className="app">
      <header className="hero">
        <p className="eyebrow">PeakProgress</p>
        <h1>Deine sportliche Entwicklung</h1>
        <p className="subtitle">
          Laufen, Bouldern und Krafttraining an einem Ort.
        </p>
      </header>

      <TrainingSessionForm
        key={editingSession?.id ?? 'new'}
        sessionToEdit={editingSession}
        onCreated={handleSessionCreated}
        onUpdated={handleSessionUpdated}
        onCancelEdit={() => setEditingSession(null)}
      />
        {analytics && <SportAnalytics analytics={analytics} />}
        
      <section className="analytics-section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Python Analytics</p>
            <h2>Deine Statistiken</h2>
          </div>

          <button
            className="refresh-button"
            type="button"
            disabled={analyticsLoading}
            onClick={() => void loadAnalytics()}
          >
            {analyticsLoading ? 'Lädt …' : 'Aktualisieren'}
          </button>
        </div>

        {analyticsLoading && !analytics && (
          <p className="status">
            Statistiken werden berechnet …
          </p>
        )}

        {analyticsError && (
          <p className="status error">{analyticsError}</p>
        )}

        {analytics && (
          <div className="analytics-grid">
            <article className="metric-card">
              <span>Einheiten insgesamt</span>
              <strong>{analytics.totalSessions}</strong>
            </article>

            <article className="metric-card">
              <span>Trainingszeit insgesamt</span>
              <strong>
                {formatDuration(analytics.totalDurationMinutes)}
              </strong>
            </article>

            <article className="metric-card">
              <span>Durchschnittliche Dauer</span>
              <strong>
                {analytics.averageDurationMinutes.toLocaleString(
                  'de-DE',
                  {
                    maximumFractionDigits: 1,
                  },
                )}{' '}
                Min.
              </strong>
            </article>

            {trainingTypes.map((trainingType) => (
              <article
                className={`metric-card metric-${trainingType.toLowerCase()}`}
                key={trainingType}
              >
                <span>{typeLabels[trainingType]}</span>
                <strong>
                  {analytics.sessionsByType[trainingType] ?? 0}
                </strong>
                <small>
                  {formatDuration(
                    analytics.durationByType[trainingType] ?? 0,
                  )}
                </small>
              </article>
            ))}
          </div>
        )}
      </section>

      <section className="dashboard">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Übersicht</p>
            <h2>Trainingseinheiten</h2>
          </div>

          <span className="session-count">
            {sessions.length} Einheiten
          </span>
        </div>

        {loading && <p className="status">Daten werden geladen …</p>}

        {error && <p className="status error">{error}</p>}

        {!loading && !error && sessions.length === 0 && (
          <div className="empty-state">
            <h3>Noch keine Trainingseinheiten</h3>
            <p>
              Deine erste Einheit wartet darauf, eingetragen zu werden.
            </p>
          </div>
        )}

        {!loading && !error && sessions.length > 0 && (
          <div className="session-list">
            {sessions.map((session) => (
              <article className="session-card" key={session.id}>
                <div>
                  <span
                    className={`type type-${session.type.toLowerCase()}`}
                  >
                    {typeLabels[session.type]}
                  </span>

                  <h3>{formatDate(session.trainingDate)}</h3>

                  {session.notes && <p>{session.notes}</p>}
                  <TrainingSessionDetails session={session} />
                </div>

                <div className="session-actions">
                  <strong>{session.durationMinutes} Min.</strong>

                  <div className="card-buttons">
                    <button
                      className="edit-button"
                      type="button"
                      onClick={() => {
                        setEditingSession(session)
                        window.scrollTo({
                          top: 0,
                          behavior: 'smooth',
                        })
                      }}
                    >
                      Bearbeiten
                    </button>

                    <button
                      className="delete-button"
                      type="button"
                      disabled={deletingId === session.id}
                      onClick={() =>
                        void handleSessionDeleted(session.id)
                      }
                    >
                      {deletingId === session.id
                        ? 'Löscht …'
                        : 'Löschen'}
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>
    </main>
  )
}

export default App