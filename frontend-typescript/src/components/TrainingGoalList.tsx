import type {
  GoalMetric,
  TrainingGoal,
} from '../types/goal'

interface TrainingGoalListProps {
  goals: TrainingGoal[]
  loading: boolean
  error: string | null
  deletingId: number | null
  onEdit: (goal: TrainingGoal) => void
  onDelete: (id: number) => void
}

const metricLabels: Record<GoalMetric, string> = {
  RUNNING_DISTANCE_KM: 'Laufdistanz',
  RUNNING_PACE_SECONDS_PER_KM: 'Lauf-Pace',
  BOULDERING_GRADE: 'Bouldering',
  STRENGTH_WEIGHT_KG: 'Krafttraining',
}

function formatPace(seconds: number): string {
  const roundedSeconds = Math.round(seconds)
  const minutes = Math.floor(roundedSeconds / 60)
  const remainingSeconds = roundedSeconds % 60

  return `${minutes}:${String(remainingSeconds).padStart(2, '0')} min/km`
}

function formatValue(
  value: number,
  metric: GoalMetric,
): string {
  switch (metric) {
    case 'RUNNING_DISTANCE_KM':
      return `${value.toLocaleString('de-DE', {
        maximumFractionDigits: 2,
      })} km`

    case 'RUNNING_PACE_SECONDS_PER_KM':
      return formatPace(value)

    case 'BOULDERING_GRADE':
      return `V${Math.round(value)}`

    case 'STRENGTH_WEIGHT_KG':
      return `${value.toLocaleString('de-DE', {
        maximumFractionDigits: 2,
      })} kg`
  }
}

function formatDate(date: string): string {
  return new Intl.DateTimeFormat('de-DE').format(
    new Date(`${date}T00:00:00`),
  )
}

export function TrainingGoalList({
  goals,
  loading,
  error,
  deletingId,
  onEdit,
  onDelete,
}: TrainingGoalListProps) {
  return (
    <section className="goals-section">
      <div className="section-heading">
        <div>
          <p className="eyebrow">Persönliche Ziele</p>
          <h2>Dein nächster Meilenstein</h2>
        </div>

        <span className="goal-count">
          {goals.length} {goals.length === 1 ? 'Ziel' : 'Ziele'}
        </span>
      </div>

      {loading && (
        <p className="status">Ziele werden geladen …</p>
      )}

      {error && <p className="status error">{error}</p>}

      {!loading && !error && goals.length === 0 && (
        <div className="empty-state">
          <h3>Noch keine Ziele festgelegt</h3>
          <p>
            Lege dein erstes Ziel fest und verfolge automatisch
            deinen Fortschritt.
          </p>
        </div>
      )}

      {!loading && goals.length > 0 && (
        <div className="goal-grid">
          {goals.map((goal) => {
            const progress = Math.min(
              Math.max(goal.progressPercent, 0),
              100,
            )

            return (
              <article
                className={`goal-card ${
                  goal.completed ? 'goal-completed' : ''
                }`}
                key={goal.id}
              >
                <div className="goal-card-heading">
                  <div>
                    <span
                      className={`goal-metric goal-metric-${goal.metric.toLowerCase()}`}
                    >
                      {metricLabels[goal.metric]}
                    </span>

                    <h3>{goal.title}</h3>
                  </div>

                  <span
                    className={
                      goal.completed
                        ? 'goal-status completed'
                        : 'goal-status'
                    }
                  >
                    {goal.completed
                      ? 'Erreicht'
                      : `${progress.toLocaleString('de-DE', {
                          maximumFractionDigits: 1,
                        })} %`}
                  </span>
                </div>

                {goal.exerciseName && (
                  <p className="goal-exercise">
                    Übung: {goal.exerciseName}
                  </p>
                )}

                <div className="goal-progress-track">
                  <span
                    className="goal-progress-bar"
                    style={{ width: `${progress}%` }}
                  />
                </div>

                <div className="goal-values">
                  <div>
                    <span>Aktueller Bestwert</span>
                    <strong>
                      {goal.currentValue === null
                        ? 'Noch keine Daten'
                        : formatValue(
                            goal.currentValue,
                            goal.metric,
                          )}
                    </strong>
                  </div>

                  <div>
                    <span>Zielwert</span>
                    <strong>
                      {formatValue(
                        goal.targetValue,
                        goal.metric,
                      )}
                    </strong>
                  </div>
                </div>

                <div className="goal-card-footer">
                  <span>
                    {goal.targetDate
                      ? `Zieldatum: ${formatDate(goal.targetDate)}`
                      : 'Ohne festes Zieldatum'}
                  </span>

                  <div className="card-buttons">
                    <button
                      className="edit-button"
                      type="button"
                      onClick={() => onEdit(goal)}
                    >
                      Bearbeiten
                    </button>

                    <button
                      className="delete-button"
                      type="button"
                      disabled={deletingId === goal.id}
                      onClick={() => onDelete(goal.id)}
                    >
                      {deletingId === goal.id
                        ? 'Löscht …'
                        : 'Löschen'}
                    </button>
                  </div>
                </div>
              </article>
            )
          })}
        </div>
      )}
    </section>
  )
}