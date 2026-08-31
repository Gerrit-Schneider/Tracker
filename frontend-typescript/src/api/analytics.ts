export interface AnalyticsSummary {
  totalSessions: number
  totalDurationMinutes: number
  averageDurationMinutes: number
  sessionsByType: Record<string, number>
  durationByType: Record<string, number>

  totalRunningDistanceKm: number
  averageRunningPaceSecondsPerKm: number | null

  totalCompletedBoulders: number
  completedBouldersByGrade: Record<string, number>

  totalStrengthVolumeKg: number
  strengthVolumeByExercise: Record<string, number>
  highestVolumeExercise: string | null
}

export interface AnalyticsProgressPoint {
  trainingDate: string
  sessionCount: number
  totalDurationMinutes: number
  runningDistanceKm: number
  averageRunningPaceSecondsPerKm: number | null
  completedBoulders: number
  strengthVolumeKg: number
}

const ANALYTICS_API_URL =
  import.meta.env.VITE_ANALYTICS_API_URL ?? 'http://localhost:8000'

async function fetchAnalytics<T>(path: string): Promise<T> {
  const response = await fetch(`${ANALYTICS_API_URL}${path}`)

  if (!response.ok) {
    throw new Error(
      `Analytics request failed with status ${response.status}`,
    )
  }

  return response.json() as Promise<T>
}

export function getAnalyticsSummary(): Promise<AnalyticsSummary> {
  return fetchAnalytics<AnalyticsSummary>(
    '/api/analytics/summary',
  )
}

export function getAnalyticsProgress(): Promise<
  AnalyticsProgressPoint[]
> {
  return fetchAnalytics<AnalyticsProgressPoint[]>(
    '/api/analytics/progress',
  )
}