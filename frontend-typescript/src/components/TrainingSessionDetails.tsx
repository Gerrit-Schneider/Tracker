import type {
  RunType,
  TrainingSession,
} from '../types/training'

interface TrainingSessionDetailsProps {
  session: TrainingSession
}

const runTypeLabels: Record<RunType, string> = {
  EASY: 'Lockerer Lauf',
  TEMPO: 'Tempolauf',
  INTERVAL: 'Intervalle',
  LONG_RUN: 'Langer Lauf',
  RECOVERY: 'Regenerationslauf',
  RACE: 'Wettkampf',
  OTHER: 'Sonstiger Lauf',
}

function formatTime(totalSeconds: number): string {
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor((totalSeconds % 3600) / 60)
  const seconds = totalSeconds % 60

  if (hours > 0) {
    return [
      String(hours),
      String(minutes).padStart(2, '0'),
      String(seconds).padStart(2, '0'),
    ].join(':')
  }

  return [
    String(minutes),
    String(seconds).padStart(2, '0'),
  ].join(':')
}

function formatNumber(value: number): string {
  return value.toLocaleString('de-DE', {
    maximumFractionDigits: 2,
  })
}

export function TrainingSessionDetails({
  session,
}: TrainingSessionDetailsProps) {
  if (session.type === 'RUNNING' && session.runningDetails) {
    const details = session.runningDetails

    return (
      <div className="session-details running-summary">
        <div>
          <span>Laufart</span>
          <strong>{runTypeLabels[details.runType]}</strong>
        </div>

        <div>
          <span>Distanz</span>
          <strong>
            {formatNumber(details.distanceMeters / 1000)} km
          </strong>
        </div>

        <div>
          <span>Laufzeit</span>
          <strong>{formatTime(details.elapsedSeconds)}</strong>
        </div>

        <div>
          <span>Pace</span>
          <strong>
            {formatTime(details.paceSecondsPerKilometer)} min/km
          </strong>
        </div>

        {details.averageHeartRate !== null && (
          <div>
            <span>Ø Herzfrequenz</span>
            <strong>{details.averageHeartRate} bpm</strong>
          </div>
        )}

        {details.maxHeartRate !== null && (
          <div>
            <span>Max. Herzfrequenz</span>
            <strong>{details.maxHeartRate} bpm</strong>
          </div>
        )}
      </div>
    )
  }

  if (
    session.type === 'BOULDERING' &&
    session.boulderingResults.length > 0
  ) {
    return (
      <div className="session-details bouldering-summary">
        {session.boulderingResults.map((result) => (
          <div className="grade-result" key={result.id}>
            <strong>{result.grade}</strong>
            <span>
              {result.completedCount}/{result.attemptedCount}{' '}
              geschafft
            </span>
          </div>
        ))}
      </div>
    )
  }

  if (
    session.type === 'STRENGTH' &&
    session.strengthExercises.length > 0
  ) {
    return (
      <div className="session-details strength-summary">
        {session.strengthExercises.map((exercise) => (
          <div className="exercise-summary" key={exercise.id}>
            <div className="exercise-summary-heading">
              <strong>{exercise.exerciseName}</strong>
              <span>
                {formatNumber(exercise.volumeKg)} kg Volumen
              </span>
            </div>

            <div className="set-summary-list">
              {exercise.sets.map((strengthSet) => (
                <span
                  className="set-summary"
                  key={strengthSet.id}
                >
                  {strengthSet.setNumber}. Satz:{' '}
                  {strengthSet.repetitions} ×{' '}
                  {formatNumber(strengthSet.weightKg)} kg
                </span>
              ))}
            </div>
          </div>
        ))}
      </div>
    )
  }

  return null
}