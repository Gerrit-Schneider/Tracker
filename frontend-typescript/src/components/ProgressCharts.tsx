import type { AnalyticsProgressPoint } from '../api/analytics'

interface ChartPoint {
  date: string
  value: number
}

interface LineChartProps {
  title: string
  description: string
  color: string
  points: ChartPoint[]
  formatValue: (value: number) => string
}

const chartWidth = 640
const chartHeight = 220
const paddingLeft = 75
const paddingRight = 25
const paddingTop = 25
const paddingBottom = 45

function formatDate(date: string): string {
  return new Intl.DateTimeFormat('de-DE', {
    day: '2-digit',
    month: '2-digit',
  }).format(new Date(`${date}T00:00:00`))
}

function formatPace(seconds: number): string {
  const roundedSeconds = Math.round(seconds)
  const minutes = Math.floor(roundedSeconds / 60)
  const remainingSeconds = roundedSeconds % 60

  return `${minutes}:${String(remainingSeconds).padStart(2, '0')}`
}

function LineChart({
  title,
  description,
  color,
  points,
  formatValue,
}: LineChartProps) {
  if (points.length === 0) {
    return (
      <article className="progress-chart-card">
        <h3>{title}</h3>
        <p className="detail-hint">Noch keine Daten vorhanden.</p>
      </article>
    )
  }

  const values = points.map((point) => point.value)
  const minimumValue = Math.min(...values)
  const maximumValue = Math.max(...values)
  const valueRange = maximumValue - minimumValue

  const drawingWidth =
    chartWidth - paddingLeft - paddingRight
  const drawingHeight =
    chartHeight - paddingTop - paddingBottom

  const positionedPoints = points.map((point, index) => {
    const x =
      points.length === 1
        ? paddingLeft + drawingWidth / 2
        : paddingLeft +
          (index / (points.length - 1)) * drawingWidth

    const y =
      valueRange === 0
        ? paddingTop + drawingHeight / 2
        : paddingTop +
          ((maximumValue - point.value) / valueRange) *
            drawingHeight

    return {
      ...point,
      x,
      y,
    }
  })

  const linePoints = positionedPoints
    .map((point) => `${point.x},${point.y}`)
    .join(' ')

  const dateLabels =
    positionedPoints.length === 1
      ? positionedPoints
      : [
          positionedPoints[0],
          positionedPoints[positionedPoints.length - 1],
        ]

  return (
    <article className="progress-chart-card">
      <h3>{title}</h3>

      <svg
        className="progress-chart"
        viewBox={`0 0 ${chartWidth} ${chartHeight}`}
        role="img"
        aria-label={`${title} im Zeitverlauf`}
      >
        {[0, 0.5, 1].map((position) => {
          const y = paddingTop + position * drawingHeight

          return (
            <line
              className="chart-grid-line"
              x1={paddingLeft}
              x2={chartWidth - paddingRight}
              y1={y}
              y2={y}
              key={position}
            />
          )
        })}

        {valueRange === 0 ? (
          <text
            className="chart-value-label"
            x={paddingLeft - 10}
            y={paddingTop + drawingHeight / 2}
          >
            {formatValue(maximumValue)}
          </text>
        ) : (
          <>
            <text
              className="chart-value-label"
              x={paddingLeft - 10}
              y={paddingTop}
            >
              {formatValue(maximumValue)}
            </text>

            <text
              className="chart-value-label"
              x={paddingLeft - 10}
              y={paddingTop + drawingHeight}
            >
              {formatValue(minimumValue)}
            </text>
          </>
        )}

        <polyline
          points={linePoints}
          fill="none"
          stroke={color}
          strokeWidth="4"
          strokeLinecap="round"
          strokeLinejoin="round"
          vectorEffect="non-scaling-stroke"
        />

        {positionedPoints.map((point, index) => (
          <circle
            cx={point.x}
            cy={point.y}
            r="6"
            fill={color}
            stroke="#ffffff"
            strokeWidth="3"
            key={`${point.date}-${index}`}
          >
            <title>
              {formatDate(point.date)}: {formatValue(point.value)}
            </title>
          </circle>
        ))}

        {dateLabels.map((point, index) => (
          <text
            className="chart-date-label"
            x={point.x}
            y={chartHeight - 12}
            textAnchor={
              positionedPoints.length === 1
                ? 'middle'
                : index === 0
                  ? 'start'
                  : 'end'
            }
            key={`${point.date}-${index}`}
          >
            {formatDate(point.date)}
          </text>
        ))}
      </svg>

      <p className="chart-description">{description}</p>
    </article>
  )
}

interface ProgressChartsProps {
  progress: AnalyticsProgressPoint[]
}

export function ProgressCharts({
  progress,
}: ProgressChartsProps) {
  const runningDistance = progress
    .filter((point) => point.runningDistanceKm > 0)
    .map((point) => ({
      date: point.trainingDate,
      value: point.runningDistanceKm,
    }))

  const runningPace = progress.flatMap((point) =>
    point.averageRunningPaceSecondsPerKm === null
      ? []
      : [
          {
            date: point.trainingDate,
            value: point.averageRunningPaceSecondsPerKm,
          },
        ],
  )

  const completedBoulders = progress
    .filter((point) => point.completedBoulders > 0)
    .map((point) => ({
      date: point.trainingDate,
      value: point.completedBoulders,
    }))

  const strengthVolume = progress
    .filter((point) => point.strengthVolumeKg > 0)
    .map((point) => ({
      date: point.trainingDate,
      value: point.strengthVolumeKg,
    }))

  return (
    <div className="progress-chart-grid">
      <LineChart
        title="Laufdistanz"
        description="Gelaufene Kilometer pro Trainingstag"
        color="#397552"
        points={runningDistance}
        formatValue={(value) => `${value.toFixed(1)} km`}
      />

      <LineChart
        title="Laufpace"
        description="Minuten pro Kilometer – niedriger ist schneller"
        color="#3b82c4"
        points={runningPace}
        formatValue={(value) => `${formatPace(value)} min/km`}
      />

      <LineChart
        title="Geschaffte Boulder"
        description="Erfolgreich abgeschlossene Boulder pro Trainingstag"
        color="#d97706"
        points={completedBoulders}
        formatValue={(value) => String(value)}
      />

      <LineChart
        title="Kraftvolumen"
        description="Bewegtes Gesamtgewicht pro Trainingstag"
        color="#8b5cf6"
        points={strengthVolume}
        formatValue={(value) =>
          `${value.toLocaleString('de-DE')} kg`
        }
      />
    </div>
  )
}