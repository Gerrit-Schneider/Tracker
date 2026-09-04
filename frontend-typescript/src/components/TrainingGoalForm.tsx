import { useState } from 'react'
import type { FormEvent } from 'react'
import {
  createTrainingGoal,
  updateTrainingGoal,
} from '../api/trainingGoals'
import {
  goalMetrics,
  type GoalMetric,
  type SaveTrainingGoal,
  type TrainingGoal,
} from '../types/goal'

interface TrainingGoalFormProps {
  goalToEdit: TrainingGoal | null
  onCreated: (goal: TrainingGoal) => void
  onUpdated: (goal: TrainingGoal) => void
  onCancelEdit: () => void
}

const metricLabels: Record<GoalMetric, string> = {
  RUNNING_DISTANCE_KM: 'Laufdistanz',
  RUNNING_PACE_SECONDS_PER_KM: 'Lauf-Pace',
  BOULDERING_GRADE: 'Bouldering-Grad',
  STRENGTH_WEIGHT_KG: 'Gewicht einer Übung',
}

function getDefaultTargetValue(metric: GoalMetric): string {
  switch (metric) {
    case 'RUNNING_DISTANCE_KM':
      return '12'
    case 'RUNNING_PACE_SECONDS_PER_KM':
      return '300'
    case 'BOULDERING_GRADE':
      return '6'
    case 'STRENGTH_WEIGHT_KG':
      return '50'
  }
}

export function TrainingGoalForm({
  goalToEdit,
  onCreated,
  onUpdated,
  onCancelEdit,
}: TrainingGoalFormProps) {
  const initialMetric =
    goalToEdit?.metric ?? 'RUNNING_DISTANCE_KM'

  const initialPace =
    goalToEdit?.metric === 'RUNNING_PACE_SECONDS_PER_KM'
      ? goalToEdit.targetValue
      : 300

  const [title, setTitle] = useState(goalToEdit?.title ?? '')
  const [metric, setMetric] =
    useState<GoalMetric>(initialMetric)

  const [targetValue, setTargetValue] = useState(
    goalToEdit
      ? String(goalToEdit.targetValue)
      : getDefaultTargetValue(initialMetric),
  )

  const [paceMinutes, setPaceMinutes] = useState(
    String(Math.floor(initialPace / 60)),
  )

  const [paceSeconds, setPaceSeconds] = useState(
    String(Math.round(initialPace % 60)),
  )

  const [exerciseName, setExerciseName] = useState(
    goalToEdit?.exerciseName ?? '',
  )

  const [targetDate, setTargetDate] = useState(
    goalToEdit?.targetDate ?? '',
  )

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function handleMetricChange(nextMetric: GoalMetric) {
    setMetric(nextMetric)
    setTargetValue(getDefaultTargetValue(nextMetric))
    setExerciseName('')

    if (nextMetric === 'RUNNING_PACE_SECONDS_PER_KM') {
      setPaceMinutes('5')
      setPaceSeconds('0')
    }
  }

  function calculateTargetValue(): number {
    if (metric !== 'RUNNING_PACE_SECONDS_PER_KM') {
      return Number(targetValue)
    }

    return Number(paceMinutes) * 60 + Number(paceSeconds)
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()
    setError(null)

    const numericTargetValue = calculateTargetValue()

    if (
      !Number.isFinite(numericTargetValue) ||
      numericTargetValue <= 0
    ) {
      setError('Bitte gib einen gültigen Zielwert ein.')
      return
    }

    if (
      metric === 'RUNNING_PACE_SECONDS_PER_KM' &&
      (Number(paceSeconds) < 0 || Number(paceSeconds) > 59)
    ) {
      setError(
        'Die Sekunden der Pace müssen zwischen 0 und 59 liegen.',
      )
      return
    }

    if (
      metric === 'STRENGTH_WEIGHT_KG' &&
      exerciseName.trim() === ''
    ) {
      setError('Bitte gib die gewünschte Übung ein.')
      return
    }

    const request: SaveTrainingGoal = {
      title: title.trim(),
      metric,
      targetValue: numericTargetValue,
      exerciseName:
        metric === 'STRENGTH_WEIGHT_KG'
          ? exerciseName.trim()
          : null,
      targetDate: targetDate || null,
    }

    setSubmitting(true)

    try {
      if (goalToEdit) {
        const updatedGoal = await updateTrainingGoal(
          goalToEdit.id,
          request,
        )

        onUpdated(updatedGoal)
      } else {
        const createdGoal = await createTrainingGoal(request)

        onCreated(createdGoal)
        setTitle('')
        setTargetDate('')
        setExerciseName('')
      }
    } catch (caughtError) {
      setError(
        caughtError instanceof Error
          ? caughtError.message
          : 'Ein unbekannter Fehler ist aufgetreten.',
      )
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <section className="goal-form-panel">
      <div>
        <p className="eyebrow">
          {goalToEdit ? 'Ziel bearbeiten' : 'Neues Ziel'}
        </p>

        <h2>
          {goalToEdit
            ? 'Trainingsziel anpassen'
            : 'Persönliches Ziel festlegen'}
        </h2>
      </div>

      <form className="goal-form" onSubmit={handleSubmit}>
        <label className="field-wide">
          Titel
          <input
            type="text"
            value={title}
            maxLength={120}
            required
            placeholder="Zum Beispiel: 12 km in einem Lauf"
            onChange={(event) => setTitle(event.target.value)}
          />
        </label>

        <label>
          Zielart
          <select
            value={metric}
            onChange={(event) =>
              handleMetricChange(
                event.target.value as GoalMetric,
              )
            }
          >
            {goalMetrics.map((goalMetric) => (
              <option value={goalMetric} key={goalMetric}>
                {metricLabels[goalMetric]}
              </option>
            ))}
          </select>
        </label>

        {metric === 'RUNNING_DISTANCE_KM' && (
          <label>
            Zieldistanz in Kilometern
            <input
              type="number"
              min="0.1"
              step="0.1"
              value={targetValue}
              required
              onChange={(event) =>
                setTargetValue(event.target.value)
              }
            />
          </label>
        )}

        {metric === 'RUNNING_PACE_SECONDS_PER_KM' && (
          <fieldset className="goal-pace-fields">
            <legend>Ziel-Pace pro Kilometer</legend>

            <label>
              Minuten
              <input
                type="number"
                min="0"
                value={paceMinutes}
                required
                onChange={(event) =>
                  setPaceMinutes(event.target.value)
                }
              />
            </label>

            <label>
              Sekunden
              <input
                type="number"
                min="0"
                max="59"
                value={paceSeconds}
                required
                onChange={(event) =>
                  setPaceSeconds(event.target.value)
                }
              />
            </label>
          </fieldset>
        )}

        {metric === 'BOULDERING_GRADE' && (
          <label>
            Zielgrad
            <select
              value={targetValue}
              onChange={(event) =>
                setTargetValue(event.target.value)
              }
            >
              {Array.from({ length: 17 }, (_, index) => {
                const grade = index + 1

                return (
                  <option value={grade} key={grade}>
                    V{grade}
                  </option>
                )
              })}
            </select>
          </label>
        )}

        {metric === 'STRENGTH_WEIGHT_KG' && (
          <>
            <label>
              Übung
              <input
                type="text"
                value={exerciseName}
                maxLength={120}
                required
                placeholder="Zum Beispiel: Klimmzug"
                onChange={(event) =>
                  setExerciseName(event.target.value)
                }
              />
            </label>

            <label>
              Zielgewicht in Kilogramm
              <input
                type="number"
                min="0.5"
                step="0.5"
                value={targetValue}
                required
                onChange={(event) =>
                  setTargetValue(event.target.value)
                }
              />
            </label>
          </>
        )}

        <label>
          Zieldatum – optional
          <input
            type="date"
            value={targetDate}
            onChange={(event) =>
              setTargetDate(event.target.value)
            }
          />
        </label>

        {error && (
          <p className="form-error field-wide">{error}</p>
        )}

        <div className="form-actions">
          <button type="submit" disabled={submitting}>
            {submitting
              ? 'Wird gespeichert …'
              : goalToEdit
                ? 'Änderungen speichern'
                : 'Ziel speichern'}
          </button>

          {goalToEdit && (
            <button
              className="secondary-button"
              type="button"
              onClick={onCancelEdit}
            >
              Abbrechen
            </button>
          )}
        </div>
      </form>
    </section>
  )
}