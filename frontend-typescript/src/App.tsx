import {
  useCallback,
  useEffect,
  useState,
} from 'react'
import {
  getAnalyticsProgress,
  getAnalyticsSummary,
  type AnalyticsProgressPoint,
  type AnalyticsSummary,
} from './api/analytics'
import {
  deleteTrainingGoal,
  getTrainingGoals,
} from './api/trainingGoals'
import {
  deleteTrainingSession,
  searchTrainingSessions,
} from './api/trainingSessions'
import { Pagination } from './components/Pagination'
import { ProgressCharts } from './components/ProgressCharts'
import { SportAnalytics } from './components/SportAnalytics'
import { TrainingDataTransfer } from './components/TrainingDataTransfer'
import { TrainingGoalForm } from './components/TrainingGoalForm'
import { TrainingGoalList } from './components/TrainingGoalList'
import { TrainingSessionDetails } from './components/TrainingSessionDetails'
import {
  TrainingSessionFilters,
  type TrainingSessionFilterValues,
} from './components/TrainingSessionFilters'
import { TrainingSessionForm } from './components/TrainingSessionForm'
import type { TrainingGoal } from './types/goal'
import type {
  TrainingSession,
  TrainingType,
} from './types/training'
import './App.css'

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

const initialFilters: TrainingSessionFilterValues = {
  type: '',
  from: '',
  to: '',
  query: '',
}

const pageSize = 5

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

  const [filters, setFilters] =
    useState<TrainingSessionFilterValues>({
      ...initialFilters,
    })

  const [appliedFilters, setAppliedFilters] =
    useState<TrainingSessionFilterValues>({
      ...initialFilters,
    })

  const [page, setPage] = useState(0)
  const [totalPages, setTotalPages] = useState(0)
  const [totalElements, setTotalElements] = useState(0)

  const [goals, setGoals] = useState<TrainingGoal[]>([])
  const [goalsLoading, setGoalsLoading] = useState(true)
  const [goalsError, setGoalsError] =
    useState<string | null>(null)
  const [deletingGoalId, setDeletingGoalId] =
    useState<number | null>(null)
  const [editingGoal, setEditingGoal] =
    useState<TrainingGoal | null>(null)

  const [analytics, setAnalytics] =
    useState<AnalyticsSummary | null>(null)
  const [progress, setProgress] =
    useState<AnalyticsProgressPoint[]>([])
  const [analyticsLoading, setAnalyticsLoading] = useState(true)
  const [analyticsError, setAnalyticsError] =
    useState<string | null>(null)

  const loadSessions = useCallback(
    async (
      requestedPage: number,
      activeFilters: TrainingSessionFilterValues,
    ) => {
      setLoading(true)
      setError(null)

      try {
        const result = await searchTrainingSessions({
          type: activeFilters.type || undefined,
          from: activeFilters.from || undefined,
          to: activeFilters.to || undefined,
          query: activeFilters.query || undefined,
          page: requestedPage,
          size: pageSize,
        })

        setSessions(result.content)
        setTotalPages(result.totalPages)
        setTotalElements(result.totalElements)
      } catch (caughtError) {
        setSessions([])
        setTotalPages(0)
        setTotalElements(0)

        setError(
          caughtError instanceof Error
            ? caughtError.message
            : 'Ein unbekannter Fehler ist aufgetreten.',
        )
      } finally {
        setLoading(false)
      }
    },
    [],
  )

  const loadGoals = useCallback(async () => {
    setGoalsLoading(true)
    setGoalsError(null)

    try {
      const result = await getTrainingGoals()
      setGoals(result)
    } catch (caughtError) {
      setGoalsError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Trainingsziele konnten nicht geladen werden.',
      )
    } finally {
      setGoalsLoading(false)
    }
  }, [])

  const loadAnalytics = useCallback(async () => {
    setAnalyticsLoading(true)
    setAnalyticsError(null)

    try {
      const [summaryResult, progressResult] = await Promise.all([
        getAnalyticsSummary(),
        getAnalyticsProgress(),
      ])

      setAnalytics(summaryResult)
      setProgress(progressResult)
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
    void loadSessions(page, appliedFilters)
  }, [appliedFilters, loadSessions, page])

  useEffect(() => {
    void loadAnalytics()
    void loadGoals()
  }, [loadAnalytics, loadGoals])

  function handleSearch() {
    setPage(0)
    setAppliedFilters({
      ...filters,
    })
  }

  function handleResetFilters() {
    const resetFilters = {
      ...initialFilters,
    }

    setFilters(resetFilters)
    setAppliedFilters(resetFilters)
    setPage(0)
  }

  function handlePageChange(newPage: number) {
    setPage(newPage)

    document
      .getElementById('training-list')
      ?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      })
  }

  function handleSessionCreated() {
    if (page === 0) {
      void loadSessions(0, appliedFilters)
    } else {
      setPage(0)
    }

    void loadAnalytics()
    void loadGoals()
  }

  function handleSessionUpdated() {
    setEditingSession(null)
    void loadSessions(page, appliedFilters)
    void loadAnalytics()
    void loadGoals()
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

      if (sessions.length === 1 && page > 0) {
        setPage(page - 1)
      } else {
        void loadSessions(page, appliedFilters)
      }

      setEditingSession((currentSession) =>
        currentSession?.id === id ? null : currentSession,
      )

      void loadAnalytics()
      void loadGoals()
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

  function handleTrainingImported() {
    if (page === 0) {
      void loadSessions(0, appliedFilters)
    } else {
      setPage(0)
    }

    void loadAnalytics()
    void loadGoals()
  }

  function handleGoalCreated(createdGoal: TrainingGoal) {
    setGoals((currentGoals) =>
      [createdGoal, ...currentGoals].sort(
        (first, second) => second.id - first.id,
      ),
    )
  }

  function handleGoalUpdated(updatedGoal: TrainingGoal) {
    setGoals((currentGoals) =>
      currentGoals.map((goal) =>
        goal.id === updatedGoal.id ? updatedGoal : goal,
      ),
    )

    setEditingGoal(null)
  }

  function handleGoalEdit(goal: TrainingGoal) {
    setEditingGoal(goal)

    document
      .getElementById('goal-form')
      ?.scrollIntoView({
        behavior: 'smooth',
        block: 'start',
      })
  }

  async function handleGoalDeleted(id: number) {
    const confirmed = window.confirm(
      'Möchtest du dieses Trainingsziel wirklich löschen?',
    )

    if (!confirmed) {
      return
    }

    setDeletingGoalId(id)
    setGoalsError(null)

    try {
      await deleteTrainingGoal(id)

      setGoals((currentGoals) =>
        currentGoals.filter((goal) => goal.id !== id),
      )

      setEditingGoal((currentGoal) =>
        currentGoal?.id === id ? null : currentGoal,
      )
    } catch (caughtError) {
      setGoalsError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Trainingsziel konnte nicht gelöscht werden.',
      )
    } finally {
      setDeletingGoalId(null)
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
        key={editingSession?.id ?? 'new-session'}
        sessionToEdit={editingSession}
        onCreated={handleSessionCreated}
        onUpdated={handleSessionUpdated}
        onCancelEdit={() => setEditingSession(null)}
      />

      <div id="goal-form">
        <TrainingGoalForm
          key={editingGoal?.id ?? 'new-goal'}
          goalToEdit={editingGoal}
          onCreated={handleGoalCreated}
          onUpdated={handleGoalUpdated}
          onCancelEdit={() => setEditingGoal(null)}
        />
      </div>

      <TrainingGoalList
        goals={goals}
        loading={goalsLoading}
        error={goalsError}
        deletingId={deletingGoalId}
        onEdit={handleGoalEdit}
        onDelete={(id) => void handleGoalDeleted(id)}
      />

      <TrainingDataTransfer
        onImported={handleTrainingImported}
      />

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
          <>
            <div className="analytics-grid">
              <article className="metric-card">
                <span>Einheiten insgesamt</span>
                <strong>{analytics.totalSessions}</strong>
              </article>

              <article className="metric-card">
                <span>Trainingszeit insgesamt</span>

                <strong>
                  {formatDuration(
                    analytics.totalDurationMinutes,
                  )}
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

            <SportAnalytics analytics={analytics} />

            <div className="section-heading progress-heading">
              <div>
                <p className="eyebrow">Verlauf</p>
                <h2>Fortschritt über Zeit</h2>
              </div>
            </div>

            <ProgressCharts progress={progress} />
          </>
        )}
      </section>

      <TrainingSessionFilters
        filters={filters}
        loading={loading}
        onChange={setFilters}
        onSearch={handleSearch}
        onReset={handleResetFilters}
      />

      <section
        className="dashboard"
        id="training-list"
      >
        <div className="section-heading">
          <div>
            <p className="eyebrow">Übersicht</p>
            <h2>Trainingseinheiten</h2>
          </div>

          <span className="session-count">
            {totalElements} Einheiten
          </span>
        </div>

        {loading && (
          <p className="status">
            Daten werden geladen …
          </p>
        )}

        {error && <p className="status error">{error}</p>}

        {!loading && !error && sessions.length === 0 && (
          <div className="empty-state">
            <h3>Keine Trainingseinheiten gefunden</h3>

            <p>
              Passe deine Filter an oder trage eine neue Einheit
              ein.
            </p>
          </div>
        )}

        {!loading && !error && sessions.length > 0 && (
          <>
            <div className="session-list">
              {sessions.map((session) => (
                <article
                  className="session-card"
                  key={session.id}
                >
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
                    <strong>
                      {session.durationMinutes} Min.
                    </strong>

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

            <Pagination
              page={page}
              totalPages={totalPages}
              totalElements={totalElements}
              loading={loading}
              onPageChange={handlePageChange}
            />
          </>
        )}
      </section>
    </main>
  )
}

export default App