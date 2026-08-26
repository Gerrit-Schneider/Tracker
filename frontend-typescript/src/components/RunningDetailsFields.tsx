import type {
  RunningDetailsInput,
  RunType,
} from '../types/training'

interface RunningDetailsFieldsProps {
  value: RunningDetailsInput
  onChange: (value: RunningDetailsInput) => void
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

function formatPace(seconds: number): string {
  const minutes = Math.floor(seconds / 60)
  const remainingSeconds = seconds % 60

  return `${minutes}:${String(remainingSeconds).padStart(2, '0')}`
}

export function RunningDetailsFields({
  value,
  onChange,
}: RunningDetailsFieldsProps) {
  const hours = Math.floor(value.elapsedSeconds / 3600)
  const minutes = Math.floor(
    (value.elapsedSeconds % 3600) / 60,
  )
  const seconds = value.elapsedSeconds % 60

  const pace =
    value.distanceMeters > 0 && value.elapsedSeconds > 0
      ? Math.round(
          value.elapsedSeconds /
            (value.distanceMeters / 1000),
        )
      : null

  function updateElapsedTime(
    nextHours: number,
    nextMinutes: number,
    nextSeconds: number,
  ) {
    onChange({
      ...value,
      elapsedSeconds:
        nextHours * 3600 +
        nextMinutes * 60 +
        nextSeconds,
    })
  }

  return (
    <fieldset className="detail-panel field-wide">
      <legend>Laufdetails</legend>

      <div className="detail-grid">
        <label>
          Laufart
          <select
            value={value.runType}
            onChange={(event) =>
              onChange({
                ...value,
                runType: event.target.value as RunType,
              })
            }
          >
            {Object.entries(runTypeLabels).map(
              ([runType, label]) => (
                <option key={runType} value={runType}>
                  {label}
                </option>
              ),
            )}
          </select>
        </label>

        <label>
          Distanz in Kilometern
          <input
            type="number"
            min="0.01"
            step="0.01"
            value={
              value.distanceMeters > 0
                ? value.distanceMeters / 1000
                : ''
            }
            required
            onChange={(event) =>
              onChange({
                ...value,
                distanceMeters: Math.round(
                  Number(event.target.value) * 1000,
                ),
              })
            }
          />
        </label>

        <div className="time-fields">
          <span>Laufzeit</span>

          <label>
            Stunden
            <input
              type="number"
              min="0"
              value={hours}
              onChange={(event) =>
                updateElapsedTime(
                  Number(event.target.value),
                  minutes,
                  seconds,
                )
              }
            />
          </label>

          <label>
            Minuten
            <input
              type="number"
              min="0"
              max="59"
              value={minutes}
              onChange={(event) =>
                updateElapsedTime(
                  hours,
                  Number(event.target.value),
                  seconds,
                )
              }
            />
          </label>

          <label>
            Sekunden
            <input
              type="number"
              min="0"
              max="59"
              value={seconds}
              onChange={(event) =>
                updateElapsedTime(
                  hours,
                  minutes,
                  Number(event.target.value),
                )
              }
            />
          </label>
        </div>

        <label>
          Durchschnittliche Herzfrequenz
          <input
            type="number"
            min="30"
            max="250"
            placeholder="Optional"
            value={value.averageHeartRate ?? ''}
            onChange={(event) =>
              onChange({
                ...value,
                averageHeartRate:
                  event.target.value === ''
                    ? null
                    : Number(event.target.value),
              })
            }
          />
        </label>

        <label>
          Maximale Herzfrequenz
          <input
            type="number"
            min="30"
            max="250"
            placeholder="Optional"
            value={value.maxHeartRate ?? ''}
            onChange={(event) =>
              onChange({
                ...value,
                maxHeartRate:
                  event.target.value === ''
                    ? null
                    : Number(event.target.value),
              })
            }
          />
        </label>
      </div>

      {pace !== null && (
        <p className="calculated-value">
          Berechnete Pace: {formatPace(pace)} min/km
        </p>
      )}
    </fieldset>
  )
}