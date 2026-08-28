import type { AnalyticsSummary } from '../api/analytics'

interface SportAnalyticsProps {
  analytics: AnalyticsSummary
}

function formatPace(seconds: number | null): string {
  if (seconds === null) {
    return 'Noch keine Daten'
  }

  const roundedSeconds = Math.round(seconds)
  const minutes = Math.floor(roundedSeconds / 60)
  const remainingSeconds = roundedSeconds % 60

  return `${minutes}:${String(remainingSeconds).padStart(2, '0')} min/km`
}

function formatNumber(value: number): string {
  return value.toLocaleString('de-DE', {
    maximumFractionDigits: 2,
  })
}

export function SportAnalytics({
  analytics,
}: SportAnalyticsProps) {
  const boulderingEntries = Object.entries(
    analytics.completedBouldersByGrade,
  )

  const strengthEntries = Object.entries(
    analytics.strengthVolumeByExercise,
  ).sort(([, firstVolume], [, secondVolume]) =>
    secondVolume - firstVolume
  )

  const largestBoulderingCount = Math.max(
    ...boulderingEntries.map(([, count]) => count),
    1,
  )

  const largestStrengthVolume = Math.max(
    ...strengthEntries.map(([, volume]) => volume),
    1,
  )

  return (
    <>
      <div className="sport-analytics-grid">
        <article className="metric-card metric-running">
          <span>Laufdistanz insgesamt</span>
          <strong>
            {formatNumber(analytics.totalRunningDistanceKm)} km
          </strong>
        </article>

        <article className="metric-card metric-running">
          <span>Durchschnittliche Pace</span>
          <strong>
            {formatPace(
              analytics.averageRunningPaceSecondsPerKm,
            )}
          </strong>
        </article>

        <article className="metric-card metric-bouldering">
          <span>Geschaffte Boulder</span>
          <strong>{analytics.totalCompletedBoulders}</strong>
        </article>

        <article className="metric-card metric-strength">
          <span>Kraftvolumen insgesamt</span>
          <strong>
            {formatNumber(analytics.totalStrengthVolumeKg)} kg
          </strong>

          {analytics.highestVolumeExercise && (
            <small>
              Meistes Volumen: {analytics.highestVolumeExercise}
            </small>
          )}
        </article>
      </div>

      <div className="analytics-breakdowns">
        <article className="breakdown-card">
          <h3>Geschaffte Boulder nach Grad</h3>

          {boulderingEntries.length === 0 ? (
            <p className="detail-hint">
              Noch keine Bouldering-Daten vorhanden.
            </p>
          ) : (
            <div className="breakdown-list">
              {boulderingEntries.map(([grade, count]) => (
                <div className="breakdown-row" key={grade}>
                  <strong>{grade}</strong>

                  <div className="breakdown-track">
                    <span
                      className="breakdown-bar bouldering-bar"
                      style={{
                        width: `${
                          (count / largestBoulderingCount) * 100
                        }%`,
                      }}
                    />
                  </div>

                  <span>{count}</span>
                </div>
              ))}
            </div>
          )}
        </article>

        <article className="breakdown-card">
          <h3>Kraftvolumen nach Übung</h3>

          {strengthEntries.length === 0 ? (
            <p className="detail-hint">
              Noch keine Krafttraining-Daten vorhanden.
            </p>
          ) : (
            <div className="breakdown-list">
              {strengthEntries.map(
                ([exerciseName, volume]) => (
                  <div
                    className="breakdown-row"
                    key={exerciseName}
                  >
                    <strong>{exerciseName}</strong>

                    <div className="breakdown-track">
                      <span
                        className="breakdown-bar strength-bar"
                        style={{
                          width: `${
                            (volume / largestStrengthVolume) *
                            100
                          }%`,
                        }}
                      />
                    </div>

                    <span>{formatNumber(volume)} kg</span>
                  </div>
                ),
              )}
            </div>
          )}
        </article>
      </div>
    </>
  )
}