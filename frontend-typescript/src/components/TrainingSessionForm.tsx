import { useState } from 'react'
import type { FormEvent } from 'react'
import {
  createTrainingSession,
  updateTrainingSession,
} from '../api/trainingSessions'
import type {
  BoulderingGradeResultInput,
  CreateTrainingSession,
  RunningDetailsInput,
  StrengthExerciseInput,
  TrainingSession,
  TrainingType,
} from '../types/training'
import { BoulderingDetailsFields } from './BoulderingDetailsFields'
import { RunningDetailsFields } from './RunningDetailsFields'
import { StrengthDetailsFields } from './GymDetailsFields'

interface TrainingSessionFormProps {
  sessionToEdit: TrainingSession | null
  onCreated: (session: TrainingSession) => void
  onUpdated: (session: TrainingSession) => void
  onCancelEdit: () => void
}

function getToday(): string {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

function getInitialRunningDetails(
  session: TrainingSession | null,
): RunningDetailsInput {
  const details = session?.runningDetails

  if (!details) {
    return {
      runType: 'EASY',
      distanceMeters: 0,
      elapsedSeconds: 0,
      averageHeartRate: null,
      maxHeartRate: null,
    }
  }

  return {
    runType: details.runType,
    distanceMeters: details.distanceMeters,
    elapsedSeconds: details.elapsedSeconds,
    averageHeartRate: details.averageHeartRate,
    maxHeartRate: details.maxHeartRate,
  }
}

function getInitialBoulderingResults(
  session: TrainingSession | null,
): BoulderingGradeResultInput[] {
  return (
    session?.boulderingResults.map((result) => ({
      grade: result.grade,
      attemptedCount: result.attemptedCount,
      completedCount: result.completedCount,
    })) ?? []
  )
}

function getInitialStrengthExercises(
  session: TrainingSession | null,
): StrengthExerciseInput[] {
  return (
    session?.strengthExercises.map((exercise) => ({
      exerciseName: exercise.exerciseName,
      sets: exercise.sets.map((strengthSet) => ({
        repetitions: strengthSet.repetitions,
        weightKg: strengthSet.weightKg,
      })),
    })) ?? []
  )
}

export function TrainingSessionForm({
  sessionToEdit,
  onCreated,
  onUpdated,
  onCancelEdit,
}: TrainingSessionFormProps) {
  const [type, setType] = useState<TrainingType>(
    sessionToEdit?.type ?? 'RUNNING',
  )
  const [trainingDate, setTrainingDate] = useState(
    sessionToEdit?.trainingDate ?? getToday(),
  )
  const [durationMinutes, setDurationMinutes] = useState(
    sessionToEdit?.durationMinutes ?? 30,
  )
  const [notes, setNotes] = useState(sessionToEdit?.notes ?? '')

  const [runningDetails, setRunningDetails] =
    useState<RunningDetailsInput>(
      getInitialRunningDetails(sessionToEdit),
    )

  const [boulderingResults, setBoulderingResults] = useState<
    BoulderingGradeResultInput[]
  >(getInitialBoulderingResults(sessionToEdit))

  const [strengthExercises, setStrengthExercises] = useState<
    StrengthExerciseInput[]
  >(getInitialStrengthExercises(sessionToEdit))

  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  function validateDetails(): string | null {
    if (type === 'RUNNING') {
      if (runningDetails.distanceMeters <= 0) {
        return 'Bitte gib eine gültige Laufdistanz ein.'
      }

      if (runningDetails.elapsedSeconds <= 0) {
        return 'Bitte gib eine gültige Laufzeit ein.'
      }

      if (
        runningDetails.averageHeartRate !== null &&
        runningDetails.maxHeartRate !== null &&
        runningDetails.maxHeartRate <
          runningDetails.averageHeartRate
      ) {
        return 'Die maximale Herzfrequenz darf nicht niedriger als der Durchschnitt sein.'
      }
    }

    if (
      type === 'BOULDERING' &&
      boulderingResults.length === 0
    ) {
      return 'Bitte füge mindestens einen Bouldergrad hinzu.'
    }

    if (
      type === 'STRENGTH' &&
      strengthExercises.length === 0
    ) {
      return 'Bitte füge mindestens eine Kraftübung hinzu.'
    }

    return null
  }

  function resetDetails() {
    setRunningDetails(
      getInitialRunningDetails(null),
    )
    setBoulderingResults([])
    setStrengthExercises([])
  }

  async function handleSubmit(
    event: FormEvent<HTMLFormElement>,
  ) {
    event.preventDefault()
    setError(null)

    const validationError = validateDetails()

    if (validationError) {
      setError(validationError)
      return
    }

    setSubmitting(true)

    const request: CreateTrainingSession = {
      type,
      trainingDate,
      durationMinutes,
      notes: notes.trim(),
      runningDetails:
        type === 'RUNNING' ? runningDetails : null,
      boulderingResults:
        type === 'BOULDERING' ? boulderingResults : [],
      strengthExercises:
        type === 'STRENGTH' ? strengthExercises : [],
    }

    try {
      if (sessionToEdit) {
        const updatedSession = await updateTrainingSession(
          sessionToEdit.id,
          request,
        )

        onUpdated(updatedSession)
      } else {
        const createdSession =
          await createTrainingSession(request)

        onCreated(createdSession)
        setNotes('')
        setDurationMinutes(30)
        resetDetails()
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
    <section className="form-panel">
      <div>
        <p className="eyebrow">
          {sessionToEdit ? 'Bearbeitung' : 'Neue Einheit'}
        </p>

        <h2>
          {sessionToEdit
            ? 'Training bearbeiten'
            : 'Training eintragen'}
        </h2>
      </div>

      <form className="session-form" onSubmit={handleSubmit}>
        <label>
          Trainingsart
          <select
            value={type}
            onChange={(event) => {
              setType(event.target.value as TrainingType)
              setError(null)
            }}
          >
            <option value="RUNNING">Laufen</option>
            <option value="BOULDERING">Bouldern</option>
            <option value="STRENGTH">Krafttraining</option>
          </select>
        </label>

        <label>
          Datum
          <input
            type="date"
            value={trainingDate}
            max={getToday()}
            required
            onChange={(event) =>
              setTrainingDate(event.target.value)
            }
          />
        </label>

        <label>
          Dauer in Minuten
          <input
            type="number"
            min="1"
            max="1440"
            value={durationMinutes}
            required
            onChange={(event) =>
              setDurationMinutes(Number(event.target.value))
            }
          />
        </label>

        {type === 'RUNNING' && (
          <RunningDetailsFields
            value={runningDetails}
            onChange={setRunningDetails}
          />
        )}

        {type === 'BOULDERING' && (
          <BoulderingDetailsFields
            value={boulderingResults}
            onChange={setBoulderingResults}
          />
        )}

        {type === 'STRENGTH' && (
          <StrengthDetailsFields
            value={strengthExercises}
            onChange={setStrengthExercises}
          />
        )}

        <label className="field-wide">
          Notizen
          <textarea
            value={notes}
            maxLength={1000}
            rows={3}
            placeholder="Wie lief das Training?"
            onChange={(event) => setNotes(event.target.value)}
          />
        </label>

        {error && (
          <p className="form-error field-wide">{error}</p>
        )}

        <div className="form-actions">
          <button type="submit" disabled={submitting}>
            {submitting
              ? 'Wird gespeichert …'
              : sessionToEdit
                ? 'Änderungen speichern'
                : 'Einheit speichern'}
          </button>

          {sessionToEdit && (
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