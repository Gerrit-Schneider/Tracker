import { useState } from 'react'
import type { FormEvent } from 'react'
import {
  createTrainingSession,
  updateTrainingSession,
} from '../api/trainingSessions'
import type {
  CreateTrainingSession,
  TrainingSession,
  TrainingType,
} from '../types/training'

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
  const [submitting, setSubmitting] = useState(false)
  const [error, setError] = useState<string | null>(null)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setSubmitting(true)
    setError(null)

    const request: CreateTrainingSession = {
      type,
      trainingDate,
      durationMinutes,
      notes: notes.trim(),
    }

    try {
      if (sessionToEdit) {
        const updatedSession = await updateTrainingSession(
          sessionToEdit.id,
          request,
        )

        onUpdated(updatedSession)
      } else {
        const createdSession = await createTrainingSession(request)

        onCreated(createdSession)
        setNotes('')
        setDurationMinutes(30)
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
            onChange={(event) =>
              setType(event.target.value as TrainingType)
            }
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
            onChange={(event) => setTrainingDate(event.target.value)}
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